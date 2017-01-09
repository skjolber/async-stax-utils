package com.github.skjolber.asyncstaxutils.filter;

import org.junit.Assert;
import org.junit.Test;

public class XMLStreamFilterFactoryTest {

	@Test
	public void test1() {
		XMLStreamFilterFactory factory = XMLStreamFilterFactory.newInstance();
		
		Assert.assertTrue(factory.createInstance() instanceof DefaultXMLStreamFilter);
	}
	
	@Test
	public void test2() {
		XMLStreamFilterFactory factory = XMLStreamFilterFactory.newInstance();
		factory.setMaxDocumentLength(1024);
		Assert.assertTrue(factory.createInstance() instanceof MaxDocumentLengthXMLStreamFilter);
	}

	@Test
	public void test3() {
		XMLStreamFilterFactory factory = XMLStreamFilterFactory.newInstance();
		factory.setMaxDocumentLength(1024);
		factory.setMaxCDATANodeLength(1024);
		Assert.assertTrue(factory.createInstance() instanceof MaxNodeLengthXmlStreamFilter);
	}

}
