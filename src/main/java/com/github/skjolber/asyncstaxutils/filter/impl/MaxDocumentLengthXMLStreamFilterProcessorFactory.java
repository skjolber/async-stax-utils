package com.github.skjolber.asyncstaxutils.filter.impl;

import java.io.ByteArrayInputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.github.skjolber.asyncstaxutils.StreamProcessor;
import com.github.skjolber.asyncstaxutils.filter.XMLStreamFilterProcessor;

public class MaxDocumentLengthXMLStreamFilterProcessorFactory extends AbstractStreamFilterProcessorFactory {

	private static Logger logger = LoggerFactory.getLogger(AbstractStreamFilterProcessorFactory.class);
	
	protected int maxDocumentLength = -1;

	public MaxDocumentLengthXMLStreamFilterProcessorFactory() {
	}

	public MaxDocumentLengthXMLStreamFilterProcessorFactory(int maxDocumentLength) {
		this.maxDocumentLength = maxDocumentLength;
	}
	
	public void setMaxDocumentLength(int maxDocumentLength) {
		this.maxDocumentLength = maxDocumentLength;
	}
	
	public int getMaxDocumentLength() {
		return maxDocumentLength;
	}

	@Override
	public StreamProcessor async(Writer writer) {
		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = asyncInputFactory.createAsyncForByteArray();
		
		XMLStreamEventFilterWriter filterWriter = new XMLStreamEventFilterWriter(writer, maxDocumentLength);
		
		try {
			return new XMLStreamFilterProcessor(reader, (XMLStreamWriter2) outputFactory.createXMLStreamWriter(filterWriter), new MaxDocumentLengthXMLStreamFilter(declaration, filterWriter));
		} catch (XMLStreamException e) {
			// should never happen
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void sync(Writer output, ByteArrayInputStream bis) {
		XMLStreamWriter2 writer = null;
		XMLStreamReader2 reader = null;
		try {
			reader = (XMLStreamReader2) asyncInputFactory.createXMLStreamReader(bis);
			
			XMLStreamEventFilterWriter filterWriter = new XMLStreamEventFilterWriter(output, maxDocumentLength);
			
			writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(filterWriter);
			
			new MaxDocumentLengthXMLStreamFilter(declaration, filterWriter).filter(reader, writer);
		} catch (XMLStreamException e) {
			logger.warn("Problem filtering XML payload", e);
			if(writer != null) {
				try {
					writer.writeComment(" FILTER END - PARSE ERROR ");
				} catch (XMLStreamException e1) {
					// ignore
				}
			}
		} finally {
			try {
				if(reader != null) reader.close();
			} catch (XMLStreamException e) {
				logger.debug("Problem closing reader", e);
			}
			try {
				if(writer != null) writer.close();
			} catch (XMLStreamException e) {
				logger.debug("Problem closing writer", e);
			}
		}
	}

}
