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

public class DelegateInputStream extends InputStream {

	private InputStream in;
	private StreamProcessor listener;
	private boolean closed = false;
	private byte[] oneByte = new byte[1];
	private DelegateStreamCallback callback;
	
	public DelegateInputStream(InputStream in, StreamProcessor listener, DelegateStreamCallback callback) {
		this.in = in;
		this.listener = listener;
		this.callback = callback;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = in.read(b, off, len);
		if(read != -1) {
			listener.payload(b, off, read);
		} else {
			closed = true;
			
			shutdown();
		}
		return read;
	}
	
	@Override
	public int read() throws IOException {
		int read = in.read();
		if(read != -1) {
			oneByte[0] = (byte)(read & 0xFF);
			listener.payload(oneByte, 0 ,1);
		} else {
			closed = true;
			
			shutdown();
		}
		
		return read;
	}

	public void close() throws IOException {
		if(!closed) {
			closed = true;
			try {
				// write the rest
				byte[] buffer = new byte[16 * 1024];
				int read;
				do {
					read = in.read(buffer, 0, buffer.length);
					if(read == -1) {
						break;
					}
					listener.payload(buffer, 0, read);
				} while(true);
				
			} finally {
				shutdown();
			}
		}
	}

	private void shutdown() {
		listener.close();
		
		try {
			in.close();
		} catch(Exception e) {
			// ignore
		}
		
		callback.closed();
	}
}
