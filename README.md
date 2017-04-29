[![Build Status](https://travis-ci.org/skjolber/async-stax-utils.svg)](https://travis-ci.org/skjolber/async-stax-utils)

# async-stax-utils
This project hosts some simple utilities for dealing with XML streams in an asynchronous way. This is achieved through the use of the [Aalto-xml] asynchronous XML-parser.

Currently the main focus is on 'passthrough' scenarios like validation and logging, in which the payload `InputStream`/`Outputstream` remains unaffected. Users of this library will benefit from

  * Passthrough Input- and Outputstream read-/write-delegates with end-of-stream callback
  * Streaming XML processing
    * Schema validation
    * Max text/CDATA-node and document-length filtering
  * Synchronous processing with asynchronous fallback based on configurable cache size.

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

# Obtain
The project is based on [Maven] and is available at Maven central repository.

Example dependency config:

```xml
<dependency>
	<groupId>com.github.skjolber</groupId>
	<artifactId>async-stax-utils</artifactId>
	<version>1.0.1</version>
</dependency>
```

# Usage
The asynchronous nature adds some processing overhead and setup complexity compared to regular synchronous approach. If you prefer skipping to code examples, see [unit tests](src/test/java/com/github/skjolber/asyncstaxutils). 

## Delegate streams
Wire up an instance of `StreamProcessor`. Then create a callback,

```java
DelegateStreamCallback callback = new DelegateStreamCallback() {
	public void closed(StreamProcessor processor, boolean success) {
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

### StreamProcessorFactory
Tying streams and filters together, `StreamProcessorFactory` takes a `Writer`, used to store the filtered result, and returns a `StreamProcessor`.

```java
StreamProcessorFactory streamProcessorFactory = ...; // init
final Writer output = new StringWriter(8 * 1024); // for use in callback
StreamProcessor streamProcessor = factory.async(output);
```

The resulting `StreamProcessor` be passed to the constructors of `DelegateOutputStream` or `DelegateInputStream`. 

### AccumulatorStreamProcessor
This processor tries to cache some data, and filter using a synchronous parser if possible. This avoids the overhead the asynchronous parser for documents of limited size. Take a factory `StreamProcessorFactory`, determine the raw stream cache size,

```java
int maxCacheLengthBytes = 1024;
```
construct the `AccumulatorStreamProcessor` using 

```java
StreamProcessor streamProcessor = new AccumulatorStreamProcessor(maxCacheLengthBytes, xmlStreamFilterFactory, output);
```

finally make the delegate input

```java
DelegateInputStream dis = new DelegateInputStream(bin, streamProcessor, callback);
```
or output

```java
DelegateOutputStream dis = new DelegateOutputStream(bin, listener, callback);
```

streams and pass them up or down your pipe.

# History
- [1.0.0]: Initial release.

[Apache 2.0]:          	http://www.apache.org/licenses/LICENSE-2.0.html
[Aalto-xml]:			https://github.com/FasterXML/aalto-xml
[issue-tracker]:       	https://github.com/skjolber/async-stax-utils/issues
[1.0.0]:                https://github.com/skjolber-async-stax-utils/releases
[Maven]:                http://maven.apache.org/
