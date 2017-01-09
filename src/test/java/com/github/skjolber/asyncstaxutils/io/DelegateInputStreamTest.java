package com.github.skjolber.asyncstaxutils.io;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
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
		
		verify(callback).closed();
		
		Assert.assertTrue(Arrays.equals(xml, listener.toByteArray()));
		
	}
}
