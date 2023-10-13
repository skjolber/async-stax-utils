module com.github.skjolber.asyncstaxutils {
	exports com.github.skjolber.asyncstaxutils.io;
	exports com.github.skjolber.asyncstaxutils.filter;
	exports com.github.skjolber.asyncstaxutils.filter.impl;
	exports com.github.skjolber.asyncstaxutils.schema;
	exports com.github.skjolber.asyncstaxutils;

	requires com.fasterxml.aalto;
	requires org.apache.commons.io;
	requires org.codehaus.stax2;
	requires org.slf4j;
}
