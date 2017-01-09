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

package com.github.skjolber.asyncstaxutils;

import java.io.ByteArrayOutputStream;
import java.io.Writer;

public class AccumulatorStreamProcessor implements StreamProcessor {

	private static class LimitByteArrayOutputStream extends ByteArrayOutputStream {
		
		public LimitByteArrayOutputStream(int size) {
			super(size);
		}

		public byte[] getBuffer() {
			return buf;
		}
		
	}
	
	private LimitByteArrayOutputStream bout;
	private int limit;
	
	private boolean limited = false;
	
	private StreamProcessorFactory factory;
	private StreamProcessor streamProcessor;
	private Writer writer;

	public AccumulatorStreamProcessor(int limit, StreamProcessorFactory factory, Writer writer) {
		this.limit = limit;
		this.factory = factory;
		this.writer = writer;
		
		if(limit == -1) {
			limit = Integer.MAX_VALUE;
		}
		bout = new LimitByteArrayOutputStream(limit);
	}

	public void payload(byte[] buffer, int offset, int length) {
		if(!limited) {
			if(bout.size() + length <= limit) {
				bout.write(buffer, offset, length);
			} else {
				limited = true;				
				streamProcessor = factory.async(writer);
				streamProcessor.payload(bout.getBuffer(), 0, bout.size());
				bout = null;
				
				streamProcessor.payload(buffer, offset, length);
			}
		} else {
			streamProcessor.payload(buffer, offset, length);
		}
	}

	public void close() {
		if(!limited) {
			limited = true;
			// initial processing
			factory.sync(writer, bout.getBuffer(), 0, bout.size());
			bout = null;
		} else {
			streamProcessor.close();
		}
	}



}
