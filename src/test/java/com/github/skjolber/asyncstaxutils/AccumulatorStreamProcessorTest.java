package com.github.skjolber.asyncstaxutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.asyncstaxutils.filter.impl.MaxNodeLengthStreamProcessorFactory;
import com.github.skjolber.asyncstaxutils.io.DelegateInputStream;
import com.github.skjolber.asyncstaxutils.io.DelegateOutputStream;
import com.github.skjolber.asyncstaxutils.io.DelegateStreamCallback;

public class AccumulatorStreamProcessorTest extends AbstractStreamTest {

	@Test
	public void testWrite() throws IOException {
		byte[] xml = IOUtils.toByteArray(getClass().getResourceAsStream("/soap/21k.xml"));
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		final Writer writer = new StringWriter();

		int limitBytes = 1024;
		final int maxDocumentLength = 8 * 1024;
		
		final int maxTextNodeLength = 10;
		final int maxCDATANodeLength = 10;

		DelegateStreamCallback callback = new DelegateStreamCallback() {
			public void closed(StreamProcessor processor, boolean succcess) {
				assertWithin(writer.toString(), maxTextNodeLength, maxCDATANodeLength, maxDocumentLength);
			}
		};
		
		MaxNodeLengthStreamProcessorFactory xmlStreamFilterFactory = new MaxNodeLengthStreamProcessorFactory();
		xmlStreamFilterFactory.setMaxDocumentLength(maxDocumentLength);
		xmlStreamFilterFactory.setMaxCDATANodeLength(maxCDATANodeLength);
		xmlStreamFilterFactory.setMaxTextNodeLength(maxTextNodeLength);
		
		StreamProcessor streamProcessor = new AccumulatorStreamProcessor(limitBytes, xmlStreamFilterFactory, writer);
		DelegateOutputStream out = new DelegateOutputStream(bout, streamProcessor, callback);
		
		int offset = 0;
		int part = 25;
		
		int length = 0;
		
		for(int i = 0; i  < xml.length; i+= part) {
			int write = Math.min(xml.length - offset, part);
			out.write(xml, offset, write);
			
			offset += write;
			
			if(writer.toString().length() != length) {
				length = writer.toString().length() ;
			}
		}

		out.close();
		
		Assert.assertTrue(Arrays.equals(xml, bout.toByteArray()));
	}	
	
	@Test
	public void testRead() throws IOException {
		byte[] xml = IOUtils.toByteArray(getClass().getResourceAsStream("/soap/21k.xml"));
		
		ByteArrayInputStream bout = new ByteArrayInputStream(xml);

		final Writer writer = new StringWriter();

		int limitBytes = 1024;
		final int maxDocumentLength = 8 * 1024;
		
		final int maxTextNodeLength = 10;
		final int maxCDATANodeLength = 10;

		DelegateStreamCallback callback = new DelegateStreamCallback() {
			public void closed(StreamProcessor processor, boolean success) {
				assertWithin(writer.toString(), maxTextNodeLength, maxCDATANodeLength, maxDocumentLength);
			}
		};
		
		MaxNodeLengthStreamProcessorFactory xmlStreamFilterFactory = new MaxNodeLengthStreamProcessorFactory();
		xmlStreamFilterFactory.setMaxDocumentLength(maxDocumentLength);
		xmlStreamFilterFactory.setMaxCDATANodeLength(maxCDATANodeLength);
		xmlStreamFilterFactory.setMaxTextNodeLength(maxTextNodeLength);

		StreamProcessor streamProcessor = new AccumulatorStreamProcessor(limitBytes, xmlStreamFilterFactory, writer);
		DelegateInputStream out = new DelegateInputStream(bout, streamProcessor, callback);
		
		byte[] byteArray = IOUtils.toByteArray(out);
		
		Assert.assertTrue(Arrays.equals(xml, byteArray));
	}		
}
