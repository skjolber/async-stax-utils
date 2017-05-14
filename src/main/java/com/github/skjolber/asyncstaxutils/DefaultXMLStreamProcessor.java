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

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;

public class DefaultXMLStreamProcessor implements StreamProcessor {

	private static Logger logger = LoggerFactory.getLogger(DefaultXMLStreamProcessor.class);

	protected AsyncXMLStreamReader<AsyncByteArrayFeeder> reader;
	
	protected boolean closed;
	protected boolean error;
	
	public DefaultXMLStreamProcessor(AsyncXMLStreamReader<AsyncByteArrayFeeder> reader) {
		this.reader = reader;
	}

	public void payload(byte[] buffer, int offset, int length) {
		if(!closed) {
			try {
				reader.getInputFeeder().feedInput(buffer, offset, length);

				process();

				if(reader.getEventType() != AsyncXMLStreamReader.EVENT_INCOMPLETE) { // end of document will never occur here
					closed = true;
				}
			} catch(Exception e) {
				handleFilterException(e);
				
				closed = true;
			} finally {
				if(closed) {
					shutdown();
				}
			}
		}
	}

	protected void handleFilterException(Exception e) {
		// this logic might be moved into filters
		// exceptions will normally be from reader, 
		// writer would normally never throw an exception
		error = true;
	}

	public void close() {
		if(!closed) {
			closed = true;
			try {
				reader.getInputFeeder().endOfInput();			
				
				process();
			} catch(Exception e) {
				handleFilterException(e);
			} finally {
				shutdown();
			}
		}
	}

	protected void shutdown() {
		try {
			reader.close();
		} catch (XMLStreamException e) {
			logger.warn("Problem closing reader" , e);
		}
	}

	public boolean isError() {
		return error;
	}
	
	/**
	 * Process reader payload, the default just reads untill and incomplete event.
	 */
	
	protected void process() throws Exception {
		// do nothing
		while(reader.getEventType() != AsyncXMLStreamReader.EVENT_INCOMPLETE && reader.hasNext()) {
			reader.next();
		}
	}


}
