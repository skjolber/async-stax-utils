package com.github.skjolber.asyncstaxutils.filter;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

public interface XMLStreamFilter {

	void filter(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException;

}
