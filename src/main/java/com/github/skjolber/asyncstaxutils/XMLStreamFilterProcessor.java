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
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLStreamWriter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.github.skjolber.asyncstaxutils.filter.XMLStreamFilter;

public class XMLStreamFilterProcessor implements StreamProcessor {

	private static Logger logger = LoggerFactory.getLogger(XMLStreamFilterProcessor.class);
	
	public static enum State {
		FULL, PARTIAL, ERROR;
	}
	
	private AsyncXMLStreamReader<AsyncByteArrayFeeder> reader;
	private XMLStreamWriter2 writer;
	private XMLStreamFilter filter;
	
	private boolean closed;
	private boolean error;
	
	public XMLStreamFilterProcessor(AsyncXMLStreamReader<AsyncByteArrayFeeder> reader, XMLStreamWriter2 writer, XMLStreamFilter filter) {
		this.reader = reader;
		this.writer = writer;
		this.filter = filter;
	}

	public void payload(byte[] buffer, int offset, int length) {
		if(!closed) {
			try {
				reader.getInputFeeder().feedInput(buffer, offset, length);
				
				filter.filter(reader, writer);

				if(reader.getEventType() != AsyncXMLStreamReader.EVENT_INCOMPLETE) { // end of document will never occur here
					closed = true;
				}
			} catch(XMLStreamException e) {
				handleFilterException(e);
				
				closed = true;
			} finally {
				if(closed) {
					shutdown();
				}
			}
		}
	}

	protected void handleFilterException(XMLStreamException e) {
		// this logic might be moved into filters
		// exceptions will normally be from reader, 
		// writer would normally never throw an exception
		logger.warn("Problem filtering XML payload", e);
		
		error = true;
		try {
			writer.writeComment(" FILTERING EXCEPTION OCCORED ");
		} catch (XMLStreamException e1) {
			// ignore
		}
	}

	public void close() {
		if(!closed) {
			closed = true;
			try {
				reader.getInputFeeder().endOfInput();			
				
				filter.filter(reader, writer);
			} catch(XMLStreamException e) {
				handleFilterException(e);
			} finally {
				shutdown();
			}
		}
	}

	private void shutdown() {
		try {
			reader.close();
		} catch (XMLStreamException e) {
			logger.warn("Problem closing reader" , e);
		}
		try {
			writer.close();
		} catch (XMLStreamException e) {
			logger.warn("Problem closing writer" , e);
		}
	}

	public boolean isError() {
		return error;
	}
}
