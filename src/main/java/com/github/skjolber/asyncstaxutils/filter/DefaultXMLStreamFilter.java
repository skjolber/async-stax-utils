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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public class DefaultXMLStreamFilter extends AbstractXMLStreamFilter {

	public DefaultXMLStreamFilter(boolean declaration) {
		super(declaration);
	}

	public void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		// get the first event so we can print the declaration
		while(reader.hasNext()) {
			int event = reader.next();
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:

				writeStartElement(reader, writer);
				
				writeAttributes(reader, writer);
				
				break;
			case XMLStreamConstants.END_ELEMENT:
				writer.writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS: {
					writer.writeCharacters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
				}
				break;
			case XMLStreamConstants.COMMENT:
				writer.writeComment(reader.getText());
				break;
			case XMLStreamConstants.CDATA: {
					writer.writeCData(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
				}
				
				break;
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
				break;
			case XMLStreamConstants.ENTITY_REFERENCE:
			case XMLStreamConstants.ENTITY_DECLARATION:
			case XMLStreamConstants.ATTRIBUTE:
			case XMLStreamConstants.NAMESPACE:
				throw new IllegalArgumentException("Unsupported event " + event);
			case XMLStreamConstants.PROCESSING_INSTRUCTION: {
				writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
				break;
			}
			case AsyncXMLStreamReader.EVENT_INCOMPLETE : {
				return;
			}			
			}
		}
	}

}
