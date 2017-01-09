package com.github.skjolber.asyncstaxutils.filter;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;

public class DefaultXMLStreamFilterTest extends AbstractStreamTest {

	@Test
	public void testElements1() throws Exception {
		String[] resources = new String[]{"/filter/element1.xml", "/filter/element2.xml", "/filter/element3.xml"};
		for(String resource : resources) {
			validate(resource);
		}
	}
	
	private void validate(String resource) throws Exception {
		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = inputFactory.createAsyncForByteArray();
		
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter2 writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(stringWriter);
		
		byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(resource));
		reader.getInputFeeder().feedInput(byteArray, 0, byteArray.length);
		reader.getInputFeeder().endOfInput();
		
		DefaultXMLStreamFilter filter = new DefaultXMLStreamFilter(true);

		filter.filter(reader, writer);

		writer.close();
		
		Assert.assertEquals(new String(byteArray, "UTF-8"), stringWriter.toString());
	}	
}
