/***************************************************************************
 * Copyright 2017 Thomas Rorvik Skjolberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.github.skjolber.asyncstaxutils.filter.impl;

import java.io.ByteArrayInputStream;
import java.io.Writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.codehaus.stax2.XMLOutputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.asyncstaxutils.StreamProcessor;
import com.github.skjolber.asyncstaxutils.filter.StreamFilterProcessorFactory;

public abstract class AbstractStreamFilterProcessorFactory implements StreamFilterProcessorFactory {

	protected final AsyncXMLInputFactory asyncInputFactory;
	protected final XMLOutputFactory2 outputFactory;
	
	protected boolean declaration;
	
	public AbstractStreamFilterProcessorFactory() {
		asyncInputFactory = new InputFactoryImpl();
		asyncInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		asyncInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);

		outputFactory = new OutputFactoryImpl();
		outputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		outputFactory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, false);
	}

	public abstract StreamProcessor async(Writer writer);

	public void sync(Writer output, byte[] buffer, int offset, int length) {
		sync(output, new ByteArrayInputStream(buffer, offset, length));
	}
	
	protected abstract void sync(Writer output, ByteArrayInputStream bis);
	
	public void setDeclaration(boolean declaration) {
		this.declaration = declaration;
	}
	
	public boolean isDeclaration() {
		return declaration;
	}
}
