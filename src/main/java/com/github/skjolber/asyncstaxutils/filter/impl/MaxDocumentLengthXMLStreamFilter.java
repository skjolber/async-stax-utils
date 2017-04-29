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

import java.io.IOException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.github.skjolber.asyncstaxutils.filter.AbstractXmlStreamFilter;

public class MaxDocumentLengthXMLStreamFilter extends AbstractXmlStreamFilter {
	
	protected final XMLStreamEventFilterWriter eventSizeFilter;
	
	protected int count = 0;
	
	public MaxDocumentLengthXMLStreamFilter(boolean declaration, XMLStreamEventFilterWriter calculator) {
		super(declaration);
		this.eventSizeFilter = calculator;
	}
	
	public static int elementSize(XMLStreamReader2 reader) throws XMLStreamException {
		String localName = reader.getLocalName();
		int count = "</>".length(); // wrapper end elements
		
		String prefix = reader.getPrefix();
		if(!isEmpty(prefix)) {
			count += (prefix.length() + 1);
		}
		return count + localName.length();
	}
	
	public void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		while(reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT: {

				count += elementSize(reader);

				writeStartElement(reader, writer);
				writeAttributes(reader, writer);
				
				// TODO close start element in a better way
				writer.writeCharacters("");
				break;
			}
			case XMLStreamConstants.END_ELEMENT:
				count -= elementSize(reader);
				
				writer.writeFullEndElement();
				break;
			case XMLStreamConstants.CHARACTERS: {
				writer.writeCharacters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
			}
			break;
			case XMLStreamConstants.COMMENT: {
				writer.writeComment(reader.getText());

				break;
			}
			case XMLStreamConstants.CDATA: {
				writer.writeCData(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
				
				break;
			}
			case XMLStreamConstants.START_DOCUMENT: {
				// does not support 'standalone', but noone uses it
				if(declaration) {
					if (!isEmpty(reader.getVersion())) {
						writer.writeStartDocument(reader.getCharacterEncodingScheme(), reader.getVersion());
					}
				}
				break;
			}
			case XMLStreamConstants.END_DOCUMENT:
				writer.writeEndDocument();
				
				return;
			case XMLStreamConstants.PROCESSING_INSTRUCTION: {
				writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());

				break;
			}
			case AsyncXMLStreamReader.EVENT_INCOMPLETE : {
				return;
			}
			default:
				throw new IllegalArgumentException("Unsupported event " + event);
			}

			writer.flush();
			if(!eventSizeFilter.accept(count)) {
				if(event == XMLStreamConstants.START_ELEMENT) {
					// this start element was too long in combination with end element
					writer.writeEndElement();
				}
				eventSizeFilter.clear();
				
				writer.writeComment(FILTER_END_MESSAGE);
				
				return;
			} else {
				try {
					eventSizeFilter.forward();
				} catch (IOException e) {
					throw new XMLStreamException(e);
				}
			}
		}	
	}

}
