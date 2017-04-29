package com.github.skjolber.asyncstaxutils.io;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.asyncstaxutils.StreamProcessor;
import com.github.skjolber.asyncstaxutils.filter.AbstractStreamTest;

public class DelegateOutputStreamTest extends AbstractStreamTest {

	@Test
	public void testDelegate() throws Exception {
		
		byte[] xml = IOUtils.toByteArray(getClass().getResourceAsStream("/soap/21k.xml"));
		
		ByteArrayOutputStream bin = new ByteArrayOutputStream();
		
		ByteArrayStreamProcessor listener = new ByteArrayStreamProcessor();
		DelegateStreamCallback callback = mock(DelegateStreamCallback.class);
		DelegateOutputStream dis = new DelegateOutputStream(bin, listener, callback);
		
		int chunk = 100;
		int offset = 0;
		
		for(int i = 0; i  < xml.length; i+= chunk) {
			int write = Math.min(xml.length - offset, chunk);
			dis.write(xml, offset, write);
			
			offset += write;
		}
		
		byte[] byteArray = bin.toByteArray();
		
		Assert.assertTrue(Arrays.equals(xml, byteArray));
		
		dis.close();
		
		verify(callback).closed(listener, true);

		Assert.assertTrue(Arrays.equals(xml, listener.toByteArray()));

	}
	
	@Test
	public void testDelegateWithException() throws Exception {
		
		OutputStream bin = mock(OutputStream.class);
		
		ByteArrayStreamProcessor listener = new ByteArrayStreamProcessor();
		DelegateStreamCallback callback = mock(DelegateStreamCallback.class);
		DelegateOutputStream dis = new DelegateOutputStream(bin, listener, callback);
		
		byte[] buffer = new byte[4 * 1024];
		doThrow(new IOException()).when(bin).write(buffer, 0, buffer.length);

		try {
			dis.write(buffer);
		} catch(Exception e) {
			
		} finally {
			dis.close();
		}
		verify(callback).closed(listener, false);
	}
	
	
	
}
