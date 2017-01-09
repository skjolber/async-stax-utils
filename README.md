# async-stax-utils
This project hosts some simple utilities for dealing with XML streams in an asynchronous way. This is achieved through the use of the [Aalto-xml] asynchronous XML-parser.

Users of this library will benefit from

  * Input- and Outputstream read-/write-delegates with callback
  * XML filtering
    * Max text- and CDATA-node length
    * Max document length
  * Synchronous processing with asynchronous fallback based on configurable cache size.

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

# Obtain
The project is based on [Maven] and is pending release to  Maven central repository.

Example dependency config:

```xml
<dependency>
	<groupId>com.github.skjolber</groupId>
	<artifactId>async-stax-utils</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```

# Usage
The asynchronous nature adds some processing overhead and setup complexity compared to regular synchronous approach. If you prefer skipping to code examples, see [unit tests](src/test/java/com/github/skjolberg). 

## Delegate streams
Wire up an instance of `StreamProcessor`. Then create a callback,

```java
DelegateStreamCallback callback = new DelegateStreamCallback() {
	public void closed() {
		System.out.println("Stream closed");
	}
};
```
for end-of-stream logic. Wrap `OutputStream` and/or `InputStream` instances using

```java
OutputStream dos = new DelegateOutputStream(out, streamProcessor, callback);
```
or input
```java
InputStream dis = new DelegateOutputStream(in, streamProcessor, callback);
```
and pass the wrappers up/down your processing pipe.

## Filters
Filtering is performed via the `XMLStreamFilter` interface, which consists of a single method

```java
void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException;
```

### XMLStreamFilterFactory
This factory class supports 4 parameters,

  * XML declaration
  * Max length
    * Document
    * Text-node
    * CDATA-node

Limits are enforced in code points, i.e. readable characters.

```java
XMLStreamFilterFactory xmlStreamFilterFactory = new XMLStreamFilterFactory();

// configure limits
xmlStreamFilterFactory.setMaxDocumentSize(maxDocumentSize);
xmlStreamFilterFactory.setMaxCDATANodeLength(maxCDATANodeLength);
xmlStreamFilterFactory.setMaxTextNodeLength(maxTextNodeLength);

// create filter
XMLStreamFilter filter = xmlStreamFilterFactory.createInstance();
```

The resulting filter is usually for one-time use, and holds internally a state.

### StreamProcessorFactory
Tying streams and filters together, `StreamProcessorFactory` takes a `Writer`, used to store the filtered result, and returns a `StreamProcessor`.

```java
StreamProcessorFactory streamProcessorFactory = new DefaultStreamProcessorFactory(xmlStreamFilterFactory);
final Writer output = new StringWriter(8 * 1024); // for use in callback
StreamProcessor streamProcessor = factory.async(output);
```

The resulting `StreamProcessor` be passed to the constructors of `DelegateOutputStream` or `DelegateInputStream`. 

### AccumulatorStreamProcessor
This processor tries to cache some data, and filter using a synchronous parser if possible. This avoids the overhead the asynchronous parser for documents of limited size. Create a factory

```java
XMLStreamFilterFactory xmlStreamFilterFactory = XMLStreamFilterFactory.newInstance();

// configure limits
xmlStreamFilterFactory.setMaxDocumentSize(maxDocumentSize);
xmlStreamFilterFactory.setMaxCDATANodeLength(maxCDATANodeLength);
xmlStreamFilterFactory.setMaxTextNodeLength(maxTextNodeLength);

// pass on to stream processor factory
StreamProcessorFactory factory = new AsyncStreamProcessorFactory(xmlStreamFilterFactory);
```

determine the raw stream cache size,

```java
int maxCacheLengthBytes = 1024;
```
and add an output Writer and callback as above. Finally initialize with

```java
StreamProcessor streamProcessor = new AccumulatorStreamProcessor(maxCacheLengthBytes, factory, output);
```

and again pass this instance to to the constructors of `DelegateOutputStream` or `DelegateInputStream`. 

# History
- [1.0.0]: Initial release.

[Apache 2.0]:          	http://www.apache.org/licenses/LICENSE-2.0.html
[Aalto-xml]:			https://github.com/FasterXML/aalto-xml
[issue-tracker]:       	https://github.com/skjolber/async-stax-utils/issues
[1.0.0]:                https://github.com/skjolber-async-stax-utils/releases
[Maven]:                http://maven.apache.org/