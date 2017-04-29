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

import com.github.skjolber.asyncstaxutils.filter.XMLStreamFilter;
import com.github.skjolber.asyncstaxutils.filter.XMLStreamFilterFactory;

public class DefaultXMLStreamFilterFactory implements XMLStreamFilterFactory {

	public static DefaultXMLStreamFilterFactory newInstance() {
		return new DefaultXMLStreamFilterFactory();
	}

	protected boolean declaration;
	protected int maxDocumentLength = -1;
	protected int maxTextNodeLength = -1; // not always in use, if so set to max int
	protected int maxCDATANodeLength = -1;  // not always in use, if so set to max int

	private XMLStreamWriterLengthEstimator estimator = new XMLStreamWriterLengthEstimator();
	
	public XMLStreamFilter createInstance() {
		XMLStreamFilter filter;
		if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
			filter = new MaxNodeLengthXmlStreamFilter(declaration, maxTextNodeLength, maxCDATANodeLength, maxDocumentLength, estimator);
		} else if(maxDocumentLength != -1) {
			filter = new MaxDocumentLengthXMLStreamFilter(declaration, maxDocumentLength, estimator);
		} else {
			filter = new DefaultXMLStreamFilter(declaration);
		}
		return filter;
	}

	public boolean isDeclaration() {
		return declaration;
	}

	public void setDeclaration(boolean declaration) {
		this.declaration = declaration;
	}

	public int getMaxDocumentLength() {
		return maxDocumentLength;
	}

	public void setMaxDocumentLength(int maxDocumentLength) {
		this.maxDocumentLength = maxDocumentLength;
	}

	public int getMaxTextNodeLength() {
		return maxTextNodeLength;
	}

	public void setMaxTextNodeLength(int maxTextNodeLength) {
		this.maxTextNodeLength = maxTextNodeLength;
	}

	public int getMaxCDATANodeLength() {
		return maxCDATANodeLength;
	}

	public void setMaxCDATANodeLength(int maxCDATANodeLength) {
		this.maxCDATANodeLength = maxCDATANodeLength;
	}

	public XMLStreamWriterLengthEstimator getEstimator() {
		return estimator;
	}

	public void setEstimator(XMLStreamWriterLengthEstimator estimator) {
		this.estimator = estimator;
	}
	
}
