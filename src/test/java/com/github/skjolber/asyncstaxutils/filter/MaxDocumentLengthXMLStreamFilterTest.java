package com.github.skjolber.asyncstaxutils.filter;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;

public class MaxDocumentLengthXMLStreamFilterTest extends AbstractStreamTest {

	@Test
	public void testElements1() throws Exception {
		String[] resources = new String[]{"/filter/element1.xml", "/filter/element2.xml", "/filter/element3.xml"};
		for(String resource : resources) {
			validateDelta(resource, 0);
		}
	}
	
	@Test
	public void testElements2() throws Exception {
		String[] resources = new String[]{"/filter/element1.xml", "/filter/element2.xml", "/filter/element3.xml"};
		for(String resource : resources) {
			validateDelta(resource, -3);
		}
	}
	
	@Test
	public void testCDATA() throws Exception {
		String[] resources = new String[]{"/filter/cdata1.xml", "/filter/cdata2.xml"};
		for(String resource : resources) {			
			validate(resource, 17);
		}
	}
	
	@Test
	public void testText() throws Exception {
		String[] resources = new String[]{"/filter/text1.xml", "/filter/text2.xml"};
		for(String resource : resources) {
			validate(resource, 17);
		}
	}

	@Test
	public void testProcessingInstruction() throws Exception {
		String[] resources = new String[]{"/filter/processingInstruction1.xml"};
		for(String resource : resources) {
			validate(resource, 17);
		}
	}	

	private void validateDelta(String resource, int count) throws Exception {
		validate(resource, IOUtils.toByteArray(getClass().getResourceAsStream(resource)).length + count);
	}

	private void validate(String resource, int count) throws Exception {
		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = inputFactory.createAsyncForByteArray();
		
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter2 writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(stringWriter);
		
		byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
		reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
		
		XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 

		MaxDocumentLengthXMLStreamFilter filter = new MaxDocumentLengthXMLStreamFilter(false, count, estimator);

		filter.filter(reader, writer);

		writer.close();
		
		Assert.assertEquals(resource, count, stringWriter.toString().length());
	}	
		
}
