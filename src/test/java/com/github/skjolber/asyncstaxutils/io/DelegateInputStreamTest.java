package com.github.skjolber.asyncstaxutils.io;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.asyncstaxutils.filter.AbstractStreamTest;

public class DelegateInputStreamTest extends AbstractStreamTest {

	@Test
	public void testDelegate() throws Exception {
		
		byte[] xml = IOUtils.toByteArray(getClass().getResourceAsStream("/soap/21k.xml"));
		
		ByteArrayInputStream bin = new ByteArrayInputStream(xml);
		
		ByteArrayStreamProcessor listener = new ByteArrayStreamProcessor();
		DelegateStreamCallback callback = mock(DelegateStreamCallback.class);
		DelegateInputStream dis = new DelegateInputStream(bin, listener, callback);
		
		byte[] byteArray = IOUtils.toByteArray(dis);
		
		Assert.assertTrue(Arrays.equals(xml, byteArray));
		
		dis.close();
		
		verify(callback).closed(listener, true);
		
		Assert.assertTrue(Arrays.equals(xml, listener.toByteArray()));
		
	}
	
	@Test
	public void testDelegatePrematureClose() throws Exception {
		
		byte[] xml = IOUtils.toByteArray(getClass().getResourceAsStream("/soap/21k.xml"));
		
		ByteArrayInputStream bin = new ByteArrayInputStream(xml);
		
		ByteArrayStreamProcessor listener = new ByteArrayStreamProcessor();
		DelegateStreamCallback callback = mock(DelegateStreamCallback.class);
		DelegateInputStream dis = new DelegateInputStream(bin, listener, callback, true);

		dis.read(new byte[4 * 1024]);
		
		dis.close();
		
		verify(callback).closed(listener, true);
		
		Assert.assertTrue(Arrays.equals(xml, listener.toByteArray()));
		
	}
	
	@Test
	public void testDelegateWithExceptionOnReadByte() throws Exception {
		InputStream bin = mock(InputStream.class);
		
		ByteArrayStreamProcessor listener = new ByteArrayStreamProcessor();
		DelegateStreamCallback callback = mock(DelegateStreamCallback.class);
		DelegateInputStream dis = new DelegateInputStream(bin, listener, callback);
		when(bin.read()).thenThrow(new IOException());

		try {
			dis.read();
		} catch(Exception e) {
			// ignore
		} finally {
			dis.close();
		}
		verify(callback).closed(listener, false);
	}
	
	@Test
	public void testDelegateWithExceptionOnReadArray() throws Exception {
		InputStream bin = mock(InputStream.class);
		
		ByteArrayStreamProcessor listener = new ByteArrayStreamProcessor();
		DelegateStreamCallback callback = mock(DelegateStreamCallback.class);
		DelegateInputStream dis = new DelegateInputStream(bin, listener, callback);
		
		byte[] buffer = new byte[4 * 1024];
		when(bin.read(buffer, 0, buffer.length)).thenThrow(new IOException());

		try {
			dis.read(buffer);
		} catch(Exception e) {
			// ignore
		} finally {
			dis.close();
		}
		verify(callback).closed(listener, false);
	}
}
