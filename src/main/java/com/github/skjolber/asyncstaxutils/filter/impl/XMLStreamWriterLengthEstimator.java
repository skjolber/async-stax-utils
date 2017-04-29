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

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;

public class XMLStreamWriterLengthEstimator {

	public int startElement(XMLStreamReader2 reader) throws XMLStreamException {
		String localName = reader.getLocalName();
		int count = "<></>".length(); // wrapper start and end elements
		
		String prefix = reader.getPrefix();
		if(!isEmpty(prefix)) {
			count += (prefix.length() + 1) * 2;
		}
		
		for (int i = 0; i < reader.getNamespaceCount(); i++) {
			String nsURI = reader.getNamespaceURI(i);
			String nsPrefix = reader.getNamespacePrefix(i);

			count += " xmlns=\"\"".length(); // space, equals and wrappers
			count += nsURI.length();
			if(!isEmpty(nsPrefix)) {
				count += 1; // :
				count += nsPrefix.length();
			}
		}
		
		return count + localName.length() * 2;
	}

	public int endElement(XMLStreamReader2 reader) throws XMLStreamException {
		return 0;
	}

	public int attributes(XMLStreamReader2 reader) throws XMLStreamException {
		int count = 0;
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			count += attribute(reader, i);
		}
		return count;
	}

	public int attribute(XMLStreamReader2 reader, int i, String value) throws XMLStreamException {
		String ns = reader.getAttributeNamespace(i);
		String nsPrefix = reader.getAttributePrefix(i);
		
		int count = 1; // space
		if(!isEmpty(ns) && !isEmpty(nsPrefix)) {
			// xmlns
			count += nsPrefix.length();
			count += 1; // colon
		} else if(nsPrefix != null && !nsPrefix.isEmpty()){
			count += nsPrefix.length();
		}
		count += reader.getAttributeLocalName(i).length();
		count += 2; // equals, wrapper start
		count += countEncoded(value);
		count += 1; // wrapper end
		
		return count;
	}

	public int attribute(XMLStreamReader2 reader, int i) throws XMLStreamException {
		return attribute(reader, i, reader.getAttributeValue(i));
	}

	protected int countEncoded(String value) {
		// http://stackoverflow.com/questions/439298/best-way-to-encode-text-data-for-xml-in-java
		int count = value.length();
		for(int k = 0; k < value.length(); k++) {
			char c = value.charAt(k);
			
			if (c < 256) {
	            count = encodedCount(count, c);
			}
		}
		return count;
	}

	public static int countEncoded(char[] value, int offset, int length) {
		// http://stackoverflow.com/questions/439298/best-way-to-encode-text-data-for-xml-in-java
		// https://github.com/FasterXML/aalto-xml/blob/0f801121285205eaefcc3c00ad243c6e680ed02b/src/main/java/com/fasterxml/aalto/out/CharXmlWriter.java#L1445
		int count = length;
		for(int k = offset; k < offset + length; k++) {
			if (value[k] < 256) {
	            count = encodedCount(count, value[k]);
			}
		}
		return count;
	}
	
	public static int countEncoded(String string, int limit) {
		
		char[] value = string.toCharArray();
		
		int count = 0;
		int k = 0;
		do {
			int charLength;
			if (value[k] < 256) {
	            charLength = encodedCount(value[k]);
			} else {
				charLength = 1;
			}
			
            if(count + charLength >= limit) {
            	return k;
            }
            count += charLength;
			
			k++;
		} while(k < value.length);
		
		return k;
	}

	public static int encodedCount(int count, char c) {
		if (c == '&') {
		    count+= 4;
		} else if (c == '<') {
		    count+= 3;
		} else if (c == '>') {
		    count+= 3;
		} else if (c == '\'') {
		    count+= 5;
		} else if (c == '"') {
		    count+= 5;
		}
		return count;
	}

	public static int encodedCount(char c) {
		if (c == '&') {
		   return 5;
		} else if (c == '<') {
			return 4;
		} else if (c == '>') {
			return 4;
		} else if (c == '\'') {
			return 6;
		} else if (c == '"') {
			return 6;
		}
		return 0;
	}

	public int comment(XMLStreamReader2 reader) throws XMLStreamException {
		String text = reader.getText();

		return "<!---->".length() + text.length();
	}

	public int processingInstruction(XMLStreamReader2 reader) throws XMLStreamException {
		int count = "<? ?>".length();
		String piData = reader.getPIData();
		if(piData != null) {
			count += piData.length();
		}
		String piTarget = reader.getPITarget();
		if(piTarget != null) {
			count += piTarget.length();
		}
		return count;
	}

	public int xmlDeclaration(XMLStreamReader2 reader) throws XMLStreamException {
		if(reader.getCharacterEncodingScheme() != null || reader.getVersion() != null) {
			return "<?xml version='' encoding=''?>".length() + reader.getCharacterEncodingScheme().length() + reader.getVersion().length();
		}
		return 0;
	}

	public int cdata(XMLStreamReader2 reader) throws XMLStreamException {
		int count = "<![CDATA[]]>".length();
		String s = reader.getText();
		if (s != null) {
			return count + s.length();
		}
		return count;
	}

	public int text(XMLStreamReader2 reader) throws XMLStreamException {
		return countEncoded(reader.getTextCharacters(), 0, reader.getTextLength());
	}

	public int characters(String string) throws XMLStreamException {
		return countEncoded(string);
	}
	
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}
}
