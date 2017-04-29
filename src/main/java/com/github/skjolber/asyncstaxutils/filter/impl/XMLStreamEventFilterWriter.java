package com.github.skjolber.asyncstaxutils.filter.impl;

import java.io.IOException;
import java.io.Writer;

/**
 * A cached writer with max-length and manual flushing to the underlying stream.
 */

public class XMLStreamEventFilterWriter extends Writer {

	private Writer delegate;
	private int size = 0;
	private int maxLength;
	
	private int cacheOffset = 0;
	private char[] cache;
			
	public XMLStreamEventFilterWriter(Writer delegate, int maxLength) {
		this.delegate = delegate;
		this.maxLength = maxLength;
		this.cache = new char[maxLength];
	}
	
	public int getCacheOffset() {
		return cacheOffset;
	}
	
	public void writeCache() throws IOException {
		delegate.write(cache, 0, cacheOffset);
		
		cacheOffset = 0;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if(maxLength < Integer.MAX_VALUE) {
			int range = Math.min(len, maxLength - cacheOffset);
			System.arraycopy(cbuf, off, cache, cacheOffset, range);
			
			cacheOffset += range;
		} else {
			delegate.write(cbuf, off, len);
		}
		
		size += len;
	}
	
	public void forward() throws IOException {
		if(cacheOffset > 0) {
			delegate.write(cache, 0, cacheOffset);
			
			cacheOffset = 0;
		}
	}
	
	public void clear() {
		cacheOffset = 0;
		maxLength = Integer.MAX_VALUE;
	}
	
	public boolean accept(int delta) {
		return size + delta <= maxLength;
	}
	
	@Override
	public void close() throws IOException {
		delegate.close();
	}
	
	@Override
	public void flush() throws IOException {
		delegate.close();
	}
	
	public Writer getDelegate() {
		return delegate;
	}

	 public int getSize() {
		return size;
	}

}