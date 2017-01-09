package com.github.skjolber.asyncstaxutils.schema;

import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;

import org.xml.sax.ErrorHandler;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

public class ValidationStreamProcessorFactory {

	protected final AsyncXMLInputFactory xmlInputFactory;

	protected final Schema schema;

	public ValidationStreamProcessorFactory(Schema schema) {
		this.schema = schema;
		
		xmlInputFactory = new InputFactoryImpl();
		xmlInputFactory.configureForXmlConformance();
	}

	public ValidationStreamProcessor createInstance() {
		return createInstance(null);
	}

	public ValidationStreamProcessor createInstance(ErrorHandler errorHandler) {
		AsyncXMLStreamReader<AsyncByteArrayFeeder> reader = xmlInputFactory.createAsyncForByteArray();
		ValidatorHandler validator = schema.newValidatorHandler();
		
		if(errorHandler != null) {
			validator.setErrorHandler(errorHandler);
		}
		return new ValidationStreamProcessor(reader, validator);
	}

}
