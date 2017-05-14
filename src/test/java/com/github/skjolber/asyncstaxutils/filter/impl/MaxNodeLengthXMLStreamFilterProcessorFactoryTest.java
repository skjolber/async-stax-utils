package com.github.skjolber.asyncstaxutils.filter.impl;

import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.asyncstaxutils.filter.DefaultXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxDocumentLengthXMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxNodeLengthXMLStreamFilterProcessorFactory;
import com.github.skjolber.asyncstaxutils.filter.impl.MaxNodeLengthXMLStreamFilter;

public class MaxNodeLengthXMLStreamFilterProcessorFactoryTest {

	@Test
	public void test1() {
		MaxNodeLengthXMLStreamFilterProcessorFactory factory = new MaxNodeLengthXMLStreamFilterProcessorFactory();
		
		Assert.assertTrue(factory.createInstance(null) instanceof DefaultXMLStreamFilter);
	}
	
	@Test
	public void test2() {
		MaxNodeLengthXMLStreamFilterProcessorFactory factory = new MaxNodeLengthXMLStreamFilterProcessorFactory();
		factory.setMaxDocumentLength(1024);
		Assert.assertTrue(factory.createInstance(null) instanceof MaxDocumentLengthXMLStreamFilter);
	}

	@Test
	public void test3() {
		MaxNodeLengthXMLStreamFilterProcessorFactory factory = new MaxNodeLengthXMLStreamFilterProcessorFactory();
		factory.setMaxDocumentLength(1024);
		factory.setMaxCDATANodeLength(1024);
		Assert.assertTrue(factory.createInstance(null) instanceof MaxNodeLengthXMLStreamFilter);
	}

}
