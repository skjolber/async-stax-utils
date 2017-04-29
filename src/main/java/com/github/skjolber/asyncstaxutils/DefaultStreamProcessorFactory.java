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

import java.io.ByteArrayInputStream;
import java.io.Writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.asyncstaxutils.filter.XMLStreamFilterFactory;

public class DefaultStreamProcessorFactory implements StreamProcessorFactory {

	private static Logger logger = LoggerFactory.getLogger(DefaultStreamProcessorFactory.class);
	
	private final AsyncXMLInputFactory asyncInputFactory;
	private final XMLOutputFactory2 outputFactory;
	private final XMLStreamFilterFactory factory;
	
	public DefaultStreamProcessorFactory(XMLStreamFilterFactory factory) {
		this.factory = factory;
		
		asyncInputFactory = new InputFactoryImpl();
		asyncInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		asyncInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);

		outputFactory = new OutputFactoryImpl();
		outputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		outputFactory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, false);
	}

	public StreamProcessor async(Writer writer) {
		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
		try {
			return new XMLStreamFilterProcessor(reader, (XMLStreamWriter2) outputFactory.createXMLStreamWriter(writer), factory.createInstance());
		} catch (XMLStreamException e) {
			// should never happen
			throw new RuntimeException(e);
		}
	}

	public void sync(Writer output, byte[] buffer, int offset, int length) {
		
		XMLStreamWriter2 writer = null;
		XMLStreamReader2 reader = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer, offset, length);
			writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(output);
			reader = (XMLStreamReader2) asyncInputFactory.createXMLStreamReader(bis);
			
			factory.createInstance().filter(reader, writer);
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
