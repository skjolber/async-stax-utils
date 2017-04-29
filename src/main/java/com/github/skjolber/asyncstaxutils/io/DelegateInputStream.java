/***************************************************************************
 * Copyright 2017 Thomas Rorvik Skjolberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.github.skjolber.asyncstaxutils.io;

import java.io.IOException;
import java.io.InputStream;

import com.github.skjolber.asyncstaxutils.StreamProcessor;

/**
 * 
 * Pass-through delegate for {@linkplain InputStream} which feeds stream content into a {@linkplain StreamProcessor}. 
 * The purpose of this class is to be able to process content without filtering it. 
 * 
 * Keeps track of underlying stream state and optionally reads the remaining stream content on {@linkplain InputStream#close()}.
 *
 */

public class DelegateInputStream extends InputStream {

	private InputStream in;
	private StreamProcessor listener;
	private boolean closed = false;
	private byte[] oneByte = new byte[1];
	private DelegateStreamCallback callback;

	private boolean exception = false;
	private boolean delegateOnClose;

	public DelegateInputStream(InputStream in, StreamProcessor listener, DelegateStreamCallback callback, boolean delegateOnClose) {
		this.in = in;
		this.listener = listener;
		this.callback = callback;
		this.delegateOnClose = delegateOnClose;
	}

	public DelegateInputStream(InputStream in, StreamProcessor listener, DelegateStreamCallback callback) {
		this(in, listener, callback, false);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			int read = in.read(b, off, len);
			if(read != -1) {
				listener.payload(b, off, read);
				
				return read;
			}
		} catch(IOException | RuntimeException e) {
			exception = true;
			
			throw e;
		}
		delegateOnClose = false;
		
		return -1;
	}
	
	@Override
	public int read() throws IOException {
		try {
			int read = in.read();
			if(read != -1) {
				oneByte[0] = (byte)(read & 0xFF);
				listener.payload(oneByte, 0 ,1);
				
				return read;
			}
		} catch(IOException | RuntimeException e) {
			exception = true;
			
			throw e;
		}
		delegateOnClose = false;
		
		return -1;
	}

	public void close() throws IOException {
		if(!closed) {
			try {
				if(delegateOnClose && !exception) {
					// read/delegate the rest
					byte[] buffer = new byte[16 * 1024];
					while(read(buffer, 0, buffer.length) != -1);
				}
			} finally {
				shutdown();
			}
		}
	}

	private void shutdown() throws IOException {
		closed = true;
		
		listener.close();
		
		try {
			in.close();
		} catch(IOException | RuntimeException e) {
			exception = true;
			
			throw e;			
		} finally {
			callback.closed(listener, !exception);
		}
		
	}
}
