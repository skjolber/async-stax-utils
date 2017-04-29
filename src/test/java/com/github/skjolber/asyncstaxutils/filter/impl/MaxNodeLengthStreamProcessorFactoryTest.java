package com.github.skjolber.asyncstaxutils.filter.impl;

import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.asyncstaxutils.filter.DefaultXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxDocumentLengthXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxNodeLengthStreamProcessorFactory;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxNodeLengthXmlStreamFilter;

public class MaxNodeLengthStreamProcessorFactoryTest {

	@Test
	public void test1() {
		MaxNodeLengthStreamProcessorFactory factory = new MaxNodeLengthStreamProcessorFactory();
		
		Assert.assertTrue(factory.createInstance(null) instanceof DefaultXMLStreamFilter);
	}
	
	@Test
	public void test2() {
		MaxNodeLengthStreamProcessorFactory factory = new MaxNodeLengthStreamProcessorFactory();
		factory.setMaxDocumentLength(1024);
		Assert.assertTrue(factory.createInstance(null) instanceof MaxDocumentLengthXMLStreamFilter);
	}

	@Test
	public void test3() {
		MaxNodeLengthStreamProcessorFactory factory = new MaxNodeLengthStreamProcessorFactory();
		factory.setMaxDocumentLength(1024);
		factory.setMaxCDATANodeLength(1024);
		Assert.assertTrue(factory.createInstance(null) instanceof MaxNodeLengthXmlStreamFilter);
	}

}
