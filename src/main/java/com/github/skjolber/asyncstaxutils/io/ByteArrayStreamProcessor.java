package com.github.skjolber.asyncstaxutils.io;

import java.io.ByteArrayOutputStream;

import com.github.skjolber.asyncstaxutils.StreamProcessor;

public class ByteArrayStreamProcessor implements StreamProcessor {

	private ByteArrayOutputStream bout = new ByteArrayOutputStream();

	public ByteArrayStreamProcessor() {
		this(16 * 1024);
	}
	
	public ByteArrayStreamProcessor(int size) {
		bout = new ByteArrayOutputStream(size);
	}
	
	public void payload(byte[] buffer, int offset, int length) {
		bout.write(buffer, offset, length);
	}

	public void close() {
	}
	
	public byte[] toByteArray() {
		return bout.toByteArray();
	}
}
