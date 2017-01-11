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
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public class MaxNodeLengthXmlStreamFilter extends MaxDocumentLengthXMLStreamFilter {
	
	public static final String FILTER_TRUNCATE_MESSAGE = "...TRUNCATED BY ";
	
	protected final int maxTextNodeLength; // not always in use, if so set to max int
	protected final int maxCDATANodeLength;  // not always in use, if so set to max int

	public MaxNodeLengthXmlStreamFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, int maxDocumentLength, XMLStreamWriterLengthEstimator calculator) {
		super(declaration, maxDocumentLength, calculator);
		
		if(maxTextNodeLength < -1) {
			throw new IllegalArgumentException();
		}
		if(maxCDATANodeLength < -1) {
			throw new IllegalArgumentException();
		}
		
		if(maxTextNodeLength == -1) {
			this.maxTextNodeLength = Integer.MAX_VALUE;
		} else {
			this.maxTextNodeLength = maxTextNodeLength;
		}
		if(maxCDATANodeLength == -1) {
			this.maxCDATANodeLength = Integer.MAX_VALUE;
		} else {
			this.maxCDATANodeLength = maxCDATANodeLength;
		}
	}
	
	public boolean getXmlDeclaration() {
		return declaration;
	}
	
	public int getMaxCDATANodeLength() {
		if(maxCDATANodeLength == Integer.MAX_VALUE) {
			return -1;
		}
		return maxCDATANodeLength;
	}
	
	public int getMaxTextNodeLength() {
		if(maxTextNodeLength == Integer.MAX_VALUE) {
			return -1;
		}
		return maxTextNodeLength;
	}

	
	/**
	 * 
	 * Find effective length (as code points, logical letters).
	 * 
	 * @param chars input string.
	 * @param codePointCount desired code point count (assume higher than chars length)
	 * @return effective number of characters given the constraints
	 */

	
	public static int findCodePointLength(String chars, int codePointCount) {
		char[] c = new char[codePointCount];
		chars.getChars(0, codePointCount, c, 0);
		
		return findCodePointLength(c, 0, c.length, codePointCount);
	}
	
	/**
	 * 
	 * Find effective length (as code points, logical letters).
	 * 
	 * @param chars
	 * @param offset
	 * @param limit
	 * @param codePointCount desired code point count
	 * @return effective number of characters given the constraints
	 */

	public static int findCodePointLength(char[] chars, int offset, int limit, int codePointCount) {
		for(int i = offset; i < offset + codePointCount && i < limit; i++) {
	        if (chars[i] >= 0xD800) {
	        	// skip extra character, but also allow one more character
	        	i++;
	        	
	        	codePointCount++;
	        }
		}
		return codePointCount;
	}	
	
	public void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		// get the first event so we can print the declaration
		int event = reader.next();
		do {
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
				writer.writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS: {
				String s = reader.getText();
				
				if(!ignoreCharacters) {
					append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					characterType = XMLStreamConstants.CHARACTERS;
					
					if(this.count + length() > maxDocumentLength) {
						writer.writeComment(FILTER_END_MESSAGE);
						
						return;
					}
					
					if(length() >= 2 * maxTextNodeLength) {
						ignoreCharacters = true;
					}
				} else {
					ignoredCharacters += reader.getTextLength();
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
				String s = reader.getText();
				
				if(!ignoreCharacters) {
					append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					characterType = XMLStreamConstants.CDATA;

					if(this.count + 12 + length() > maxDocumentLength) {
						writer.writeComment(FILTER_END_MESSAGE);
						
						return;
					}
					
					if(length() >= 2 * maxCDATANodeLength) {
						ignoreCharacters = true;
					}
				} else {
					ignoredCharacters += reader.getTextLength();
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
				break;
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

			if(!reader.hasNext()) {
				break;
			}
			event = reader.next();
		} while (true);		

		return;
	}

	protected boolean handleCharacterState(XMLStreamWriter writer) throws XMLStreamException {
		String s = getCharacters();

		if(characterType == XMLStreamConstants.CHARACTERS) {
			if(s.length() > maxTextNodeLength || ignoredCharacters > 0) {
				int length = findCodePointLength(s, maxTextNodeLength);
				if(s.length() > length || ignoredCharacters > 0) {
					char[] part = new char[length];
					s.getChars(0, length, part, 0);

					int count = XMLStreamWriterLengthEstimator.countEncoded(part, 0, length) + FILTER_TRUNCATE_MESSAGE.length() + number(s.length() - length + ignoredCharacters);

					if(this.count + count > maxDocumentLength) {
						writer.writeComment(FILTER_END_MESSAGE);
						
						return false;
					}
					increment(count);
					
					writer.writeCharacters(part, 0, length);
					writer.writeCharacters(FILTER_TRUNCATE_MESSAGE);
					writer.writeCharacters(Integer.toString(s.length() - length + ignoredCharacters));
					
					return true;
				}
			}
			

			int count = calculator.countEncoded(s);

			if(this.count + count > maxDocumentLength) {
				writer.writeComment(FILTER_END_MESSAGE);
				
				return false;
			}

			increment(count);

			writer.writeCharacters(s);
		} else if(characterType == XMLStreamConstants.CDATA) {
			if(s.length() > maxCDATANodeLength || ignoredCharacters > 0) {
				int findMaxTextNodeLength = findCodePointLength(s, maxCDATANodeLength);
				if(s.length() > findMaxTextNodeLength || ignoredCharacters > 0) {
					
					int count = 12 + s.length() - (s.length() - findMaxTextNodeLength) + FILTER_TRUNCATE_MESSAGE.length() + + number(s.length() - findMaxTextNodeLength + ignoredCharacters);

					if(this.count + count > maxDocumentLength) {
						writer.writeComment(FILTER_END_MESSAGE);
						
						return false;
					}
					increment(count);

					writer.writeCData(s.substring(0, findMaxTextNodeLength) + FILTER_TRUNCATE_MESSAGE + Integer.toString(s.length() - findMaxTextNodeLength + ignoredCharacters));
					
					return true;
				}
			}
			
			int count = s.length() + 12;

			if(this.count + count > maxDocumentLength) {
				writer.writeComment(FILTER_END_MESSAGE);
				
				return false;
			}

			increment(count);

			writer.writeCData(s);
		}

		return true;
	}

	private int number(int i) {
		return 0;
	}
	
}
