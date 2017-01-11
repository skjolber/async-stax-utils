package com.github.skjolber.asyncstaxutils.schema;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.github.skjolber.asyncstaxutils.StreamProcessor;
import com.github.skjolber.asyncstaxutils.io.DelegateInputStream;
import com.github.skjolber.asyncstaxutils.io.DelegateStreamCallback;

public class ValidationStreamProcessorFactoryTest {

	private ValidationStreamProcessorFactory factory;
	
	@Before
	public void setup() throws Exception {
		InputStream resourceAsStream = getClass().getResourceAsStream("/validator/BankCustomerService.xsd");
		Assert.assertNotNull(resourceAsStream);
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(new SAXSource(new InputSource(resourceAsStream)));

		factory = new ValidationStreamProcessorFactory(schema);
	}
	
	@Test
	public void testParseValid() throws Exception {
		byte[] xml = IOUtils.toByteArray(getClass().getResourceAsStream("/validator/getAccountsRequest1.xml"));
		ByteArrayInputStream bin = new ByteArrayInputStream(xml);
		
		final DefaultErrorHandler errorHandler = new DefaultErrorHandler();
		
		ValidationStreamProcessor streamProcessor = factory.createInstance(errorHandler);
		
		DelegateStreamCallback callback = new DelegateStreamCallback() {
			
			@Override
			public void closed(StreamProcessor processor) {
				Assert.assertFalse(errorHandler.isWarning());
				Assert.assertFalse(errorHandler.isError());
				Assert.assertFalse(errorHandler.isFatalError());
			}
		};
		
		DelegateStreamCallback spyCallback = spy(callback);
		DelegateInputStream dis = new DelegateInputStream(bin, streamProcessor, spyCallback);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buffer = new byte[10];
		do {
			int read = dis.read(buffer, 0, buffer.length);
			
			if(read == -1) {
				break;
			}
			bout.write(buffer, 0, read);
		} while(true);
		
		dis.close();
		
		Assert.assertTrue(Arrays.equals(xml, bout.toByteArray()));

		verify(spyCallback).closed(streamProcessor);
		
	}
	
	@Test
	public void testParseInvalid() throws Exception {
		byte[] xml = IOUtils.toByteArray(getClass().getResourceAsStream("/validator/getAccountsRequest2.xml"));
		ByteArrayInputStream bin = new ByteArrayInputStream(xml);
		
		final DefaultErrorHandler errorHandler = new DefaultErrorHandler();
		
		ValidationStreamProcessor streamProcessor = factory.createInstance(errorHandler);
		
		DelegateStreamCallback callback = new DelegateStreamCallback() {
			
			@Override
			public void closed(StreamProcessor processor) {
				Assert.assertFalse(errorHandler.isWarning());
				Assert.assertTrue(errorHandler.isError());
				Assert.assertFalse(errorHandler.isFatalError());
			}
		};
		
		DelegateStreamCallback spyCallback = spy(callback);
		DelegateInputStream dis = new DelegateInputStream(bin, streamProcessor, spyCallback);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buffer = new byte[10];
		do {
			int read = dis.read(buffer, 0, buffer.length);
			
			if(read == -1) {
				break;
			}
			bout.write(buffer, 0, read);
		} while(true);
		
		dis.close();
		
		Assert.assertTrue(Arrays.equals(xml, bout.toByteArray()));

		verify(spyCallback).closed(streamProcessor);
		
	}

}
