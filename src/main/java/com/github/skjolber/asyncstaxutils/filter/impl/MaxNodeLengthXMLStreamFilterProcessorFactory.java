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

package com.github.skjolber.asyncstaxutils.filter.impl;

import java.io.ByteArrayInputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.github.skjolber.asyncstaxutils.StreamProcessor;
import com.github.skjolber.asyncstaxutils.filter.DefaultXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.XMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.XMLStreamFilterProcessor;

public class MaxNodeLengthXMLStreamFilterProcessorFactory extends MaxDocumentLengthXMLStreamFilterProcessorFactory {

	private static Logger logger = LoggerFactory.getLogger(MaxNodeLengthXMLStreamFilterProcessorFactory.class);

	protected int maxTextNodeLength = -1; // not always in use, if so set to max int
	protected int maxCDATANodeLength = -1;  // not always in use, if so set to max int

	private XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator();
	
	public XMLStreamFilter createInstance(Writer writer) {
		XMLStreamFilter filter;
		if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
			filter = new MaxNodeLengthXMLStreamFilter(declaration, maxTextNodeLength, maxCDATANodeLength, maxDocumentLength, estimator);
		} else if(maxDocumentLength != -1) {
			filter = new MaxDocumentLengthXMLStreamFilter(declaration, new XMLStreamEventFilterWriter(writer, maxDocumentLength));
		} else {
			filter = new DefaultXMLStreamFilter(declaration);
		}
		return filter;
	}

	public boolean isDeclaration() {
		return declaration;
	}

	public void setDeclaration(boolean declaration) {
		this.declaration = declaration;
	}

	public int getMaxTextNodeLength() {
		return maxTextNodeLength;
	}

	public void setMaxTextNodeLength(int maxTextNodeLength) {
		this.maxTextNodeLength = maxTextNodeLength;
	}

	public int getMaxCDATANodeLength() {
		return maxCDATANodeLength;
	}

	public void setMaxCDATANodeLength(int maxCDATANodeLength) {
		this.maxCDATANodeLength = maxCDATANodeLength;
	}

	public XMLStreamWriterLengthEstimator getEstimator() {
		return estimator;
	}

	public void setEstimator(XMLStreamWriterLengthEstimator estimator) {
		this.estimator = estimator;
	}

	@Override
	public StreamProcessor async(Writer writer) {
		XMLStreamFilter filter;
		if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
			filter = new MaxNodeLengthXMLStreamFilter(declaration, maxTextNodeLength, maxCDATANodeLength, maxDocumentLength, estimator);
		} else if(maxDocumentLength != -1) {
			return super.async(writer);
		} else {
			filter = new DefaultXMLStreamFilter(declaration);
		}

		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
		
		try {
			return new XMLStreamFilterProcessor(reader, (XMLStreamWriter2) outputFactory.createXMLStreamWriter(writer), filter);
		} catch (XMLStreamException e) {
			// should never happen
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void sync(Writer output, ByteArrayInputStream bis) {
		if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
			sync(output, bis, new MaxNodeLengthXMLStreamFilter(declaration, maxTextNodeLength, maxCDATANodeLength, maxDocumentLength, estimator));
		} else if(maxDocumentLength != -1) {
			super.sync(output, bis);
		} else {
			sync(output, bis, new DefaultXMLStreamFilter(declaration));
		}
	}
	
	protected void sync(Writer output, ByteArrayInputStream bis, XMLStreamFilter filter) {
		XMLStreamWriter2 writer = null;
		XMLStreamReader2 reader = null;
		try {
			reader = (XMLStreamReader2) asyncInputFactory.createXMLStreamReader(bis);
			
			writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(output);
			
			filter.filter(reader, writer);
		} catch (XMLStreamException e) {
			logger.warn("Problem filtering XML payload", e);
			if(writer != null) {
				try {
					writer.writeComment(" FILTER END - PARSE ERROR ");
				} catch (XMLStreamException e1) {
					// ignore
				}
			}
		} finally {
			try {
				if(reader != null) reader.close();
			} catch (XMLStreamException e) {
				logger.debug("Problem closing reader", e);
			}
			try {
				if(writer != null) writer.close();
			} catch (XMLStreamException e) {
				logger.debug("Problem closing writer", e);
			}
		}
	}

}
