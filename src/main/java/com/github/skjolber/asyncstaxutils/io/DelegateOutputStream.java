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
import java.io.OutputStream;

import com.github.skjolber.asyncstaxutils.StreamProcessor;

/**
 * 
 * Pass-through delegate for {@linkplain OutputStream} which feeds stream content into a {@linkplain StreamProcessor}. 
 * The purpose of this class is to be able to process content without filtering it. 
 * 
 * Keeps track of underlying stream state.
 *
 */

public class DelegateOutputStream extends OutputStream {

	private OutputStream out;
	private StreamProcessor listener;
	private boolean closed = false;
	private byte[] oneByte = new byte[1];
	private DelegateStreamCallback callback;	
	private boolean exception = false;

	public DelegateOutputStream(OutputStream out, StreamProcessor listener, DelegateStreamCallback callback) {
		super();
		this.out = out;
		this.listener = listener;
		this.callback = callback;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		try {
			listener.payload(b, off, len);
			
			out.write(b, off, len);
		} catch(IOException | RuntimeException e) {
			exception = true;
			
			throw e;
		}
	}
	
	@Override
	public void write(int b) throws IOException {
		try {
			oneByte[0] = (byte)(b & 0xFF);
			listener.payload(oneByte, 0, 1);

			out.write(b);
		} catch(IOException | RuntimeException e) {
			exception = true;
			
			throw e;
		}
	}
	
	@Override
	public void close() throws IOException {
		if(!closed) {
			closed = true;
			
			try {
				listener.close();
				
				out.close();
			} catch(IOException | RuntimeException e) {
				exception = true;
				
				throw e;
			} finally {
				callback.closed(listener, !exception);
			}
		}
		
	}

}
