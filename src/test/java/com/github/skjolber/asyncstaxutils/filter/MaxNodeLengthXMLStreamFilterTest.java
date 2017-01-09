package com.github.skjolber.asyncstaxutils.filter;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.Test;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;

public class MaxNodeLengthXMLStreamFilterTest extends AbstractStreamTest {

	@Test
	public void testElementsMaxNodeLength() throws Exception {
		String[] resources = new String[]{"/filter/element1.xml", "/filter/element2.xml", "/filter/element3.xml"};
		for(String resource : resources) {
			validate(resource, 1, 255, 255);
		}
	}
	
	@Test
	public void testElementsMaxDocumentLength() throws Exception {
		String[] resources = new String[]{"/filter/element1.xml", "/filter/element2.xml", "/filter/element3.xml"};
		for(String resource : resources) {
			validateDelta(resource, 255, 255, -3);
		}
	}
	
	@Test
	public void testCDATAMaxNodeLength() throws Exception {
		String[] resources = new String[]{"/filter/cdata1.xml", "/filter/cdata2.xml"};
		for(String resource : resources) {			
			validate(resource, 255, 1, 255);
		}
	}
	
	@Test
	public void testCDATAMaxDocumentLength() throws Exception {
		String[] resources = new String[]{"/filter/cdata1.xml", "/filter/cdata2.xml"};
		for(String resource : resources) {			
			validate(resource, 255, 255, 17);
		}
	}
	
	@Test
	public void testText() throws Exception {
		String[] resources = new String[]{"/filter/text1.xml", "/filter/text2.xml"};
		for(String resource : resources) {
			validate(resource, 255, 1, 255);
		}
	}

	@Test
	public void testProcessingInstruction() throws Exception {
		String[] resources = new String[]{"/filter/processingInstruction1.xml"};
		for(String resource : resources) {
			validate(resource, 255, 255, 17);
		}
	}	

	private void validateDelta(String resource, int maxTextNodeLength, int maxCDATANodeLength, int maxDocumentLength) throws Exception {
		validate(resource, maxTextNodeLength, maxCDATANodeLength, IOUtils.toByteArray(getClass().getResourceAsStream(resource)).length + maxDocumentLength);
	}

	private void validate(String resource, int maxTextNodeLength, int maxCDATANodeLength, int maxDocumentLength) throws Exception {
		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = inputFactory.createAsyncForByteArray();
		
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter2 writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(stringWriter);
		
		byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
		reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
		
		XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 

		MaxNodeLengthXmlStreamFilter filter = new MaxNodeLengthXmlStreamFilter(false, maxTextNodeLength, maxCDATANodeLength, maxDocumentLength, estimator);

		filter.filter(reader, writer);

		writer.close();
		
		assertWithin(stringWriter.toString(), maxTextNodeLength, maxCDATANodeLength, maxDocumentLength);
	}
		
}
