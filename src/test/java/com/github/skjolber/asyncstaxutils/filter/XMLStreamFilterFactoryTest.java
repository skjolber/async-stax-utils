package com.github.skjolber.asyncstaxutils.filter;

import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.asyncstaxutils.filter.impl.DefaultXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.DefaultXMLStreamFilterFactory;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxDocumentLengthXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxNodeLengthXmlStreamFilter;

public class XMLStreamFilterFactoryTest {

	@Test
	public void test1() {
		DefaultXMLStreamFilterFactory factory = DefaultXMLStreamFilterFactory.newInstance();
		
		Assert.assertTrue(factory.createInstance() instanceof DefaultXMLStreamFilter);
	}
	
	@Test
	public void test2() {
		DefaultXMLStreamFilterFactory factory = DefaultXMLStreamFilterFactory.newInstance();
		factory.setMaxDocumentLength(1024);
		Assert.assertTrue(factory.createInstance() instanceof MaxDocumentLengthXMLStreamFilter);
	}

	@Test
	public void test3() {
		DefaultXMLStreamFilterFactory factory = DefaultXMLStreamFilterFactory.newInstance();
		factory.setMaxDocumentLength(1024);
		factory.setMaxCDATANodeLength(1024);
		Assert.assertTrue(factory.createInstance() instanceof MaxNodeLengthXmlStreamFilter);
	}

}
