package com.github.skjolber.asyncstaxutils.filter;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public interface XMLStreamFilter {

	/**
	 * Filter XML. If the reader event type is not left at {@linkplain AsyncXMLStreamReader.EVENT_INCOMPLETE}, 
	 * it is assumed that no more filtering is necessary.
	 * @param reader input
	 * @param writer output 
	 * @throws XMLStreamException
	 */
	
	void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException;

}
