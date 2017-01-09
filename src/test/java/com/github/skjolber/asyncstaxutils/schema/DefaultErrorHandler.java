package com.github.skjolber.asyncstaxutils.schema;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultErrorHandler implements ErrorHandler {

	private boolean warning = false;
	private boolean error = false;
	private boolean fatalError = false;
	
	@Override
	public void warning(SAXParseException exception) throws SAXException {
		warning = true;
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		error = true;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		fatalError = true;
	}

	public boolean isError() {
		return error;
	}
	
	public boolean isFatalError() {
		return fatalError;
	}
	
	public boolean isWarning() {
		return warning;
	}
}
