package org.eobjects.analyzer.util;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

	public void testRightTrim() throws Exception {
		assertEquals("hello", StringUtils.rightTrim("hello  "));
		assertEquals("hello", StringUtils.rightTrim("hello \t "));
		assertEquals("hello", StringUtils.rightTrim("hello"));
		assertEquals("  hello", StringUtils.rightTrim("  hello  "));
		assertEquals("", StringUtils.rightTrim(" "));
		assertEquals("", StringUtils.rightTrim(""));
	}

	public void testLeftTrim() throws Exception {
		assertEquals("hello", StringUtils.leftTrim("  hello"));
		assertEquals("hello", StringUtils.leftTrim(" \t hello"));
		assertEquals("hello", StringUtils.leftTrim("hello"));
		assertEquals("hello  ", StringUtils.leftTrim("  hello  "));
		assertEquals("", StringUtils.leftTrim(" "));
		assertEquals("", StringUtils.leftTrim(""));
	}

	public void testBuiltInTrimUsesWhitespaces() throws Exception {
		assertEquals("hello", " hello \t ".trim());
	}
}
