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

import com.fasterxml.aalto.AsyncXMLStreamReader;

public interface XMLStreamFilter {

	/**
	 * Filter XML streams. If the reader event type is not left at {@linkplain AsyncXMLStreamReader#EVENT_INCOMPLETE}, 
	 * it is assumed that no more filtering is necessary.
	 * @param reader input
	 * @param writer output 
	 * @throws XMLStreamException
	 */
	
	void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException;

}
