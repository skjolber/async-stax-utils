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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public class MaxDocumentLengthXMLStreamFilter extends AbstractStreamFilter {

	public static final String FILTER_END_MESSAGE = " FILTERED ";
	
	protected final int maxDocumentLength;
	protected final XMLStreamWriterLengthEstimator calculator;

	protected int count = 0;

	public MaxDocumentLengthXMLStreamFilter(boolean declaration, int maxDocumentLength, XMLStreamWriterLengthEstimator calculator) {
		super(declaration);
		if(maxDocumentLength == -1) {
			this.maxDocumentLength = Integer.MAX_VALUE;
		} else {
			this.maxDocumentLength = maxDocumentLength;
		}
		this.calculator = calculator;
	}
	
	public void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		while(reader.hasNext()) {
			int event = reader.next();
			
			if(characterType != 0 && event != characterType && event != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
				if(!handleCharacterState(writer)) {
					
					return;
				}
				reset();		
			}

			switch (event) {
			case XMLStreamConstants.START_ELEMENT: {

				int count = calculator.startElement(reader) + calculator.attributes(reader);

				if(this.count + count > maxDocumentLength) {
					writer.writeComment(FILTER_END_MESSAGE);
					
					return;
				}

				increment(count);

				writeStartElement(reader, writer);
				writeAttributes(reader, writer);
					
				break;
			}
			case XMLStreamConstants.END_ELEMENT:
				writer.writeFullEndElement();
				break;
			case XMLStreamConstants.CHARACTERS: {
				
				append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
				characterType = XMLStreamConstants.CHARACTERS;

				if(this.count + length() > maxDocumentLength) {
					writer.writeComment(FILTER_END_MESSAGE);
					
					return;
				}
			}
			break;
			case XMLStreamConstants.COMMENT: {
				int count = calculator.comment(reader);

				if(this.count + count > maxDocumentLength) {
					writer.writeComment(FILTER_END_MESSAGE);
					
					return;
				}

				increment(count);

				writer.writeComment(reader.getText());

				break;
			}
			case XMLStreamConstants.CDATA: {
				append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
				characterType = XMLStreamConstants.CDATA;

				if(this.count + 12 + length() > maxDocumentLength) {
					writer.writeComment(FILTER_END_MESSAGE);
					
					return;
				}

				break;
			}
			case XMLStreamConstants.START_DOCUMENT: {
				// does not support 'standalone', but noone uses it
				if(declaration) {
					if (!isEmpty(reader.getVersion())) {
						int count = calculator.xmlDeclaration(reader);
						
						if(this.count + count > maxDocumentLength) {
							writer.writeComment(FILTER_END_MESSAGE);
							
							return;
						}
	
						increment(count);
	
						writer.writeStartDocument(reader.getCharacterEncodingScheme(), reader.getVersion());
					}
				}
				break;
			}
			case XMLStreamConstants.END_DOCUMENT:
				writer.writeEndDocument();
				
				return;
			case XMLStreamConstants.PROCESSING_INSTRUCTION: {
				int count = calculator.processingInstruction(reader);

				if(this.count + count > maxDocumentLength) {
					writer.writeComment(FILTER_END_MESSAGE);
					
					return;
				}

				increment(count);

				writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());

				break;
			}
			case AsyncXMLStreamReader.EVENT_INCOMPLETE : {
				return;
			}
			default:
				throw new IllegalArgumentException("Unsupported event " + event);
			}

		}	
	}

	protected boolean handleCharacterState(XMLStreamWriter writer) throws XMLStreamException {
		if(length() == 0) {
			return true;
		}
		String s = getCharacters().toString();
		
		if(characterType == XMLStreamConstants.CHARACTERS) {
			int count = calculator.countEncoded(s);

			if(count + count > maxDocumentLength) {
				writer.writeComment(FILTER_END_MESSAGE);
				
				return false;
			}

			increment(count);

			writer.writeCharacters(s);
		} else if(characterType == XMLStreamConstants.CDATA) {
			int count = s.length() + 12;

			if(count + count > maxDocumentLength) {
				writer.writeComment(FILTER_END_MESSAGE);
				
				return false;
			}

			increment(count);

			writer.writeCData(s);
		}

		return true;
	}


	public int getLimit() {
		return maxDocumentLength;
	}

	public void increment(int count) {
		this.count += count;
	}

}
