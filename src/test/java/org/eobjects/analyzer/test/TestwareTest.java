package org.eobjects.analyzer.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class TestwareTest extends TestCase {

	public void testLoggingLevel() throws Exception {
		Logger logger = LoggerFactory.getLogger(getClass());
		assertTrue("debug level logging is disabled", logger.isDebugEnabled());
	}
}
