package com.github.skjolber.asyncstaxutils.filter;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.Assert;
import org.junit.Before;

import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;

public class AbstractStreamTest {

	protected AsyncXMLInputFactory inputFactory;
	protected XMLOutputFactory2 outputFactory;

	@Before
	public void setup() {		
		inputFactory = new InputFactoryImpl();
		inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		inputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);

		outputFactory = new OutputFactoryImpl();
		outputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		outputFactory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, false);
	}
	

	protected void assertWithin(String string, int maxTextNodeLength, int maxCDATANodeLength, int maxDocumentLength) {
		try {
			XMLStreamReader2 r = (XMLStreamReader2) inputFactory.createXMLStreamReader(new StringReader(string));
			while(r.hasNext()) {
				int event = r.next();
				if(event == XMLStreamConstants.CHARACTERS) {
					Assert.assertTrue(r.getText().indexOf("...") <= maxTextNodeLength);
				} else if(event == XMLStreamConstants.CDATA) {
					Assert.assertTrue(r.getText().indexOf("...") <= maxCDATANodeLength);
				}
			}
		} catch(XMLStreamException e) {
			throw new RuntimeException(e);
		}
		
		Assert.assertTrue(string.length() <= maxDocumentLength);
	}	

}
