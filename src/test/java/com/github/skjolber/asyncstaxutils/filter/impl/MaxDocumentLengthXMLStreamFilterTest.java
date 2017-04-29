package com.github.skjolber.asyncstaxutils.filter.impl;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.github.skjolber.asyncstaxutils.filter.impl.XMLStreamEventFilterWriter;
import com.github.skjolber.asyncstaxutils.AbstractStreamTest;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxDocumentLengthXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxNodeLengthXmlStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.XMLStreamWriterLengthEstimator;

public class MaxDocumentLengthXMLStreamFilterTest extends AbstractStreamTest {

	@Test
	public void testElements1() throws Exception {
		String[] resources = new String[]{"/filter/element1.xml", "/filter/element2.xml", "/filter/element3.xml"};
		for(String resource : resources) {
			validateDelta(resource, 0, false);
		}
	}
	
	@Test
	public void testElements2() throws Exception {
		String[] resources = new String[]{"/filter/element1.xml", "/filter/element2.xml", "/filter/element3.xml"};
		for(String resource : resources) {
			validateDelta(resource, -3, true);
		}
	}
	
	@Test
	public void testCDATA() throws Exception {
		String[] resources = new String[]{"/filter/cdata1.xml", "/filter/cdata2.xml", "/filter/cdata3.xml"};
		for(String resource : resources) {			
			validate(resource, 17, true);
		}
	}
	
	@Test
	public void testText() throws Exception {
		String[] resources = new String[]{"/filter/text1.xml", "/filter/text2.xml", "/filter/text3.xml"};
		for(String resource : resources) {
			validate(resource, 17, true);
		}
	}

	@Test
	public void testProcessingInstruction() throws Exception {
		String[] resources = new String[]{"/filter/processingInstruction1.xml"};
		for(String resource : resources) {
			validate(resource, 17, true);
		}
	}	

	private void validateDelta(String resource, int count, boolean filtered) throws Exception {
		validate(resource, IOUtils.toByteArray(getClass().getResourceAsStream(resource)).length + count, filtered);
	}

	private void validate(String resource, int count, boolean filtered) throws Exception {
		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = inputFactory.createAsyncForByteArray();
				
		StringWriter stringWriter = new StringWriter();
		XMLStreamEventFilterWriter estimatorWriter = new XMLStreamEventFilterWriter(stringWriter, count);
		
		XMLStreamWriter2 writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(estimatorWriter);
		
		byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
		reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
		reader.getInputFeeder().endOfInput();
		
		MaxDocumentLengthXMLStreamFilter filter = new MaxDocumentLengthXMLStreamFilter(false, estimatorWriter);

		filter.filter(reader, writer);

		writer.close();

		if(filtered) {
			count += MaxNodeLengthXmlStreamFilter.FILTER_END_MESSAGE.length() + 7;
		}
		
		if(!stringWriter.toString().contains(MaxNodeLengthXmlStreamFilter.FILTER_END_MESSAGE)) {
			Assert.assertEquals(XMLStreamReader.END_DOCUMENT, reader.getEventType());
		}

		Assert.assertEquals(resource + " " + stringWriter.toString(), count, stringWriter.toString().length());
	}	
		
}
