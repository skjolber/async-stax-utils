package com.github.skjolber.asyncstaxutils.schema;

import javax.xml.XMLConstants;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.github.skjolber.asyncstaxutils.DefaultXMLStreamProcessor;
import com.github.skjolber.asyncstaxutils.StreamProcessor;

public class ValidationStreamProcessor extends DefaultXMLStreamProcessor{

	private static Logger logger = LoggerFactory.getLogger(ValidationStreamProcessor.class);
	
	private static class LocationLocator implements Locator {

		private final Location location;
		
		public LocationLocator(Location location) {
			this.location = location;
		}
		
		@Override
		public String getPublicId() {
			return location.getPublicId();
		}

		@Override
		public String getSystemId() {
			return location.getSystemId();
		}

		@Override
		public int getLineNumber() {
			return location.getLineNumber();
		}

		@Override
		public int getColumnNumber() {
			return location.getColumnNumber();
		}
	};

	private final ValidatorHandler handler;
	private final LexicalHandler lexicalHandler;
	
	public ValidationStreamProcessor(AsyncXMLStreamReader<AsyncByteArrayFeeder> reader, ValidatorHandler handler) {
		this(reader, handler, null);
	}

	public ValidationStreamProcessor(AsyncXMLStreamReader<AsyncByteArrayFeeder> reader, ValidatorHandler handler, LexicalHandler lexicalHandler) {
		super(reader);
		this.handler = handler;
		this.lexicalHandler = lexicalHandler;
	}

	@Override
	protected void process() throws Exception {
		copy(reader, handler, lexicalHandler);
	}

	protected void handleFilterException(Exception e) {
		logger.warn("Unable to validate document", e);
		super.handleFilterException(e);
		
		ErrorHandler errorHandler = handler.getErrorHandler();
		if(errorHandler != null) {
			try {
				errorHandler.fatalError(new SAXParseException("Unable to validate document", new LocationLocator(reader.getLocation())));
			} catch (SAXException e1) {
				// ignore
			}
		}
	}
	public static void copy(AsyncXMLStreamReader<AsyncByteArrayFeeder> streamReader, ValidatorHandler contentHandler, LexicalHandler lexicalHandler) throws SAXException, XMLStreamException {
		while(streamReader.hasNext()) {
			int event = streamReader.next();
			switch (event) {
	        // Attributes are handled in START_ELEMENT
	        case XMLStreamConstants.ATTRIBUTE:
	            break;
	        case XMLStreamConstants.CDATA:
	        {
	            if (lexicalHandler != null) {
	                lexicalHandler.startCDATA();
	            }
	            int length = streamReader.getTextLength();
	            int start = streamReader.getTextStart();
	            char[] chars = streamReader.getTextCharacters();
	            contentHandler.characters(chars, start, length);
	            if (lexicalHandler != null) {
	                lexicalHandler.endCDATA();
	            }
	            break;
	        }
	        case XMLStreamConstants.CHARACTERS:
	        {
	            int length = streamReader.getTextLength();
	            int start = streamReader.getTextStart();
	            char[] chars = streamReader.getTextCharacters();
	            contentHandler.characters(chars, start, length);
	            break;
	        }
	        case XMLStreamConstants.SPACE:
	        {
	            int length = streamReader.getTextLength();
	            int start = streamReader.getTextStart();
	            char[] chars = streamReader.getTextCharacters();
	            contentHandler.ignorableWhitespace(chars, start, length);
	            break;
	        }
	        case XMLStreamConstants.COMMENT:
	            if (lexicalHandler != null) {
	                int length = streamReader.getTextLength();
	                int start = streamReader.getTextStart();
	                char[] chars = streamReader.getTextCharacters();
	                lexicalHandler.comment(chars, start, length);
	            }
	            break;
	        case XMLStreamConstants.DTD:
	            break;
	        case XMLStreamConstants.END_DOCUMENT:
	            contentHandler.endDocument();
	            return;
	        case XMLStreamConstants.END_ELEMENT: {
	            String uri = streamReader.getNamespaceURI();
	            String localName = streamReader.getLocalName();
	            String prefix = streamReader.getPrefix();
	            String qname = prefix != null && prefix.length() > 0 
	                ? prefix + ":" + localName : localName;
	            contentHandler.endElement(uri, localName, qname);
	            break;
	        }
	        case XMLStreamConstants.ENTITY_DECLARATION:
	        case XMLStreamConstants.ENTITY_REFERENCE:
	        case XMLStreamConstants.NAMESPACE:
	        case XMLStreamConstants.NOTATION_DECLARATION:
	            break;
	        case XMLStreamConstants.PROCESSING_INSTRUCTION:
	            break;
	        case XMLStreamConstants.START_DOCUMENT:
	            contentHandler.startDocument();
	            break;
	        case XMLStreamConstants.START_ELEMENT: {
	            String uri = streamReader.getNamespaceURI();
	            String localName = streamReader.getLocalName();
	            String prefix = streamReader.getPrefix();
	            String qname = prefix != null && prefix.length() > 0 
	                ? prefix + ":" + localName : localName;
	            contentHandler.startElement(uri == null ? "" : uri, localName, qname, getAttributes(streamReader));
	            break;
	        }
	        case AsyncXMLStreamReader.EVENT_INCOMPLETE: {
	        	return;
	        }
	        default:
	            throw new IllegalArgumentException("Unexpected event " + event);
	        }
		}
	}
	
    public static Attributes getAttributes(AsyncXMLStreamReader<AsyncByteArrayFeeder> streamReader) {
        AttributesImpl attrs = new AttributesImpl();
        // Adding namespace declaration as attributes is necessary because
        // the xalan implementation that ships with SUN JDK 1.4 is bugged
        // and does not handle the startPrefixMapping method
        for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
            String prefix = streamReader.getNamespacePrefix(i);
            String uri = streamReader.getNamespaceURI(i);
            if (uri == null) {
                uri = "";
            }
            // Default namespace
            if (prefix == null || prefix.length() == 0) {
                attrs.addAttribute(XMLConstants.DEFAULT_NS_PREFIX, 
                                   null, 
                                   XMLConstants.XMLNS_ATTRIBUTE, 
                                   "CDATA", 
                                   uri);
            } else {
                attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
                                   prefix, 
                                   XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix, 
                                   "CDATA", 
                                   uri);
            }
        }
        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            String uri = streamReader.getAttributeNamespace(i);
            String localName = streamReader.getAttributeLocalName(i);
            String prefix = streamReader.getAttributePrefix(i);
            String qName;
            if (prefix != null && prefix.length() > 0) {
                qName = prefix + ':' + localName;
            } else {
                qName = localName;
            }
            String type = streamReader.getAttributeType(i);
            String value = streamReader.getAttributeValue(i);
            if (value == null) {
                value = "";
            }

            attrs.addAttribute(uri == null ? "" : uri, localName, qName, type, value);
        }
        return attrs;
    }
    
    public ValidatorHandler getHandler() {
		return handler;
	}

}
