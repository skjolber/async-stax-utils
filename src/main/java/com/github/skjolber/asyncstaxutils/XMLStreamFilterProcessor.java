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
	private boolean completed;
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
			} catch (XMLStreamException e1) {
				logger.error("Problem feeding input", e1);
				
				throw new RuntimeException(e1);
			}
			try {
				filter();
			} catch(XMLStreamException e) {
				handleException(e);
				
				closed = true;
				
				shutdown();
			}
		}
	}

	protected void handleException(XMLStreamException e) {
		error = true;
		logger.warn("Problem parsing XML payload", e);
		try {
			writer.writeComment(" FILTER END - PARSE ERROR ");
		} catch (XMLStreamException e1) {
			// ignore
		}
	}

	public void close() {
		if(!closed) {
			closed = true;
			reader.getInputFeeder().endOfInput();			
			try {
				filter();
				
				completed = true;
			} catch(XMLStreamException e) {
				handleException(e);
			} finally {
				shutdown();
			}
		}
	}

	public void filter() throws XMLStreamException {
		filter.filter(reader, writer);
		
		if(reader.getEventType() != AsyncXMLStreamReader.EVENT_INCOMPLETE && reader.getEventType() != XMLStreamReader.END_DOCUMENT) {
			closed = true;
			
			shutdown();
		}
	}

	private void shutdown() {
		try {
			reader.close();
		} catch (XMLStreamException e) {
		}
		try {
			writer.close();
		} catch (XMLStreamException e) {
		}
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public boolean isError() {
		return error;
	}
}
