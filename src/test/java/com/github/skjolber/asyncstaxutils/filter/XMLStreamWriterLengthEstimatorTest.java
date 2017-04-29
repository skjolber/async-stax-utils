package com.github.skjolber.asyncstaxutils.filter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.github.skjolber.asyncstaxutils.filter.impl.XMLStreamWriterLengthEstimator;

public class XMLStreamWriterLengthEstimatorTest {

	private InputFactoryImpl asyncInputFactory;
	
	@Before
	public void setup() {
		asyncInputFactory = new InputFactoryImpl();
		asyncInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		asyncInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);
	}
	
	@Test
	public void testEstimatorElements() throws Exception {
		
		String[] resources = new String[]{"/estimator/element1.xml", "/estimator/element2.xml", "/estimator/element3.xml"};
		for(String resource : resources) {
			AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
			
			byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
			reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
			
			XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 
			
			reader.next();
			reader.next();
			
			Assert.assertEquals(byteArray.length, estimator.attributes(reader) + estimator.startElement(reader));
		}
	}
	
	@Test
	public void testEstimatorCDATA() throws Exception {
		
		String[] resources = new String[]{"/estimator/cdata1.xml", "/estimator/cdata2.xml"};
		for(String resource : resources) {
			AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
			
			byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
			reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
			
			XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 
			
			reader.next();
			reader.next();
			
			int count = estimator.attributes(reader) + estimator.startElement(reader);

			while(reader.next() == XMLStreamConstants.CDATA) {
				count += estimator.cdata(reader);
			}
			
			Assert.assertEquals(byteArray.length, count);
		}
	}

	@Test
	public void testEstimatorText() throws Exception {
		
		String[] resources = new String[]{"/estimator/text1.xml", "/estimator/text2.xml"};
		for(String resource : resources) {
			AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
			
			byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
			reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
			
			XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 
			
			reader.next();
			reader.next();
			
			int count = estimator.attributes(reader) + estimator.startElement(reader);
			
			while(reader.next() == XMLStreamConstants.CHARACTERS) {
				count += estimator.text(reader);
			}
			
			Assert.assertEquals(byteArray.length, count);
			
			reader.close();
		}
	}
	
	@Test
	public void testEstimatorComment() throws Exception {
		
		String[] resources = new String[]{"/estimator/comment1.xml"};
		for(String resource : resources) {
			AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
			
			byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
			reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
			
			XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 
			
			reader.next();
			reader.next();
			
			int count = estimator.attributes(reader) + estimator.startElement(reader);

			while(reader.next() == XMLStreamConstants.COMMENT) {
				count += estimator.comment(reader);
			}
			
			Assert.assertEquals(byteArray.length, count);
		}
	}
	
	@Test
	public void testEstimatorProcessingInstruction() throws Exception {
		
		String[] resources = new String[]{"/estimator/processingInstruction1.xml"};
		for(String resource : resources) {
			AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
			
			byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
			reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
			
			XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 
			
			reader.next();
			reader.next();
			
			int count = estimator.attributes(reader) + estimator.startElement(reader);

			while(reader.next() == XMLStreamConstants.PROCESSING_INSTRUCTION) {
				count += estimator.processingInstruction(reader);
			}
			
			Assert.assertEquals(byteArray.length, count);
		}
	}
	
	@Test
	public void testEstimatorXmlDeclaration() throws Exception {
		
		String[] resources = new String[]{"/estimator/xmlDeclaration1.xml"};
		for(String resource : resources) {
			AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
			
			byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
			reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
			
			XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator(); 
			
			reader.next();
			
			int count = estimator.xmlDeclaration(reader);
			
			reader.next();
			
			count += estimator.attributes(reader) + estimator.startElement(reader);

			Assert.assertEquals(byteArray.length, count);
		}
	}
		
}
