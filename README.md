[![Build Status](https://travis-ci.org/skjolber/async-stax-utils.svg)](https://travis-ci.org/skjolber/async-stax-utils)

# async-stax-utils
This project hosts some simple utilities for dealing with XML streams in an asynchronous way. This is achieved through the use of the [Aalto-xml] asynchronous XML-parser.

Currently the main focus is on 'easedropping' scenarios like validation, logging and analytics, where input/output-stream contents is unaffected for up/downstream peers.

Users of this library will benefit from

  * Passthrough Input- and Outputstream read-/write-delegates with end-of-stream callback
  * Streaming XML-processing
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

## StreamProcessor
The `StreamProcessor` is a simple listener interface for passing bytes captured from `OutputStream` and  `InputStream`.

```java
void payload(byte[] buffer, int offset, int length);
void close();
```

## Delegate streams
Create a callback,

```java
DelegateStreamCallback callback = new DelegateStreamCallback() {
	public void closed(StreamProcessor processor, boolean success) {
		System.out.println("Stream closed");
	}
}
```
for end-of-stream logic. Take a `StreamProcessor` and create a passthrough `OutputStream` and/or `InputStream` using

```java
OutputStream dos = new DelegateOutputStream(out, streamProcessor, callback);
```

or input

```java
InputStream dis = new DelegateOutputStream(in, streamProcessor, callback);
```

Then pass these up/down your processing pipe. Then `dis.read(..)` or `dos.write(..)` then invokes our `StreamProcessor` and finally `dis.close()` or `dos.close()` triggers a call to`DelegateStreamCallback`.

## StreamProcessorFactory
`StreamProcessorFactory` is a pattern for cases for capturing filtered output in a `Writer`, for example for logging. It supports both asynchronous 
and synchronous usage.
 
```java
StreamProcessorFactory streamProcessorFactory = ...; // init
final Writer output = new StringWriter(8 * 1024); // for use in callback
StreamProcessor streamProcessor = factory.async(output);
```

### Filters
Filtering is performed via the `XMLStreamFilter` interface, which consists of a single method

```java
void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException;
```

### AccumulatorStreamProcessor
This processor tries to avoid the overhead the asynchronous processing for documents of limited size. It uses a cache and only creates a (stateful) async filter for documents which exceed a certain threshold. For

```java
int maxCacheLengthBytes = 1024;
```
construct the `AccumulatorStreamProcessor` using 

```java
StreamProcessor streamProcessor = new AccumulatorStreamProcessor(maxCacheLengthBytes, streamProcessorFactory, output);
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
- [1.0.1]: Better document-size length filtering.
- 1.0.0: Initial release.

[Apache 2.0]:          	http://www.apache.org/licenses/LICENSE-2.0.html
[Aalto-xml]:			https://github.com/FasterXML/aalto-xml
[issue-tracker]:       	https://github.com/skjolber/async-stax-utils/issues
[1.0.1]:                https://github.com/skjolber-async-stax-utils/releases
[Maven]:                http://maven.apache.org/
