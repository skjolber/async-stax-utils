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

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

public abstract class AbstractStreamFilter implements XMLStreamFilter {

	protected final boolean declaration;

	protected StringBuffer characters = new StringBuffer(1024);
	protected int characterType = 0;
	protected boolean ignoreCharacters = false;
	protected int ignoredCharacters = 0;
	
	public AbstractStreamFilter(boolean declaration) {
		this.declaration = declaration;
	}
	
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}
	
	public void writeStartElement(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		String uri = reader.getNamespaceURI();
		String prefix = reader.getPrefix();
		String local = reader.getLocalName();

		writer.writeStartElement(prefix, local, uri);

		// Write out the namespaces
		for (int i = 0; i < reader.getNamespaceCount(); i++) {
			String nsURI = reader.getNamespaceURI(i);
			String nsPrefix = reader.getNamespacePrefix(i);
			
			writer.writeNamespace(nsPrefix, nsURI);
		}
	}

	public void writeAttributes(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		// Write out attributes
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String value = reader.getAttributeValue(i);

			writeAttribute(reader, writer, i, value);
		}
	}

	public void writeAttribute(XMLStreamReader2 reader, XMLStreamWriter2 writer, int i, String value) throws XMLStreamException {
		String ns = reader.getAttributeNamespace(i);
		String nsPrefix = reader.getAttributePrefix(i);
		if(ns != null && nsPrefix != null) {
			writer.writeAttribute(nsPrefix, ns, reader.getAttributeLocalName(i), value);

			// xmlns
		} else if(nsPrefix != null && !nsPrefix.isEmpty()){
			writer.writeAttribute(nsPrefix + ':' + reader.getAttributeLocalName(i), value);
		} else {
			writer.writeAttribute(reader.getAttributeLocalName(i), value);
		}
	}

	public void writeEndElement(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		writer.writeFullEndElement();
	}

	public abstract void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException;

	public void reset() {
		characters.setLength(0);
		characterType = 0;
		ignoreCharacters = false;
		ignoredCharacters = 0;
	}
	
	public String getCharacters() {
		return characters.toString();
	}
	
	public void append(String text) {
		this.characters.append(text);
	}
	
	public void append(char[] text, int offset, int length) {
		this.characters.append(text, offset, length);
	}

	public int length() {
		return characters.length();
	}
}
