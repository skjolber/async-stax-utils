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

package com.github.skjolber.asyncstaxutils.filter;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.github.skjolber.asyncstaxutils.DefaultXMLStreamProcessor;

public class XMLStreamFilterProcessor extends DefaultXMLStreamProcessor {

	private static Logger logger = LoggerFactory.getLogger(XMLStreamFilterProcessor.class);
	
	public static enum State {
		FULL, PARTIAL, ERROR;
	}
	
	private XMLStreamWriter2 writer;
	private XMLStreamFilter filter;
	
	public XMLStreamFilterProcessor(AsyncXMLStreamReader<AsyncByteArrayFeeder> reader, XMLStreamWriter2 writer, XMLStreamFilter filter) {
		super(reader);
		this.writer = writer;
		this.filter = filter;
	}


	protected void handleFilterException(XMLStreamException e) {
		super.handleFilterException(e);
		try {
			writer.writeComment(" FILTERING EXCEPTION OCCORED ");
		} catch (XMLStreamException e1) {
			// ignore
		}
	}

	protected void shutdown() {
		super.shutdown();
		try {
			writer.close();
		} catch (XMLStreamException e) {
			logger.warn("Problem closing writer" , e);
		}
	}

	public boolean isError() {
		return error;
	}

	@Override
	protected void process() throws XMLStreamException {
		filter.filter(reader, writer);
	}
}
