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
	
	public void testIsLatin() throws Exception {
		assertTrue(StringUtils.isLatin('a'));
		assertTrue(StringUtils.isLatin('z'));
		assertTrue(StringUtils.isLatin('A'));
		assertTrue(StringUtils.isLatin('Z'));
		assertTrue(StringUtils.isLatin('c'));
		assertTrue(StringUtils.isLatin('D'));
		assertFalse(StringUtils.isLatin('æ'));
		assertFalse(StringUtils.isLatin('á'));
		assertFalse(StringUtils.isLatin('Æ'));
		assertFalse(StringUtils.isLatin('Ä'));
	}
	
	public void testIsDiacritic() throws Exception {
		assertTrue(StringUtils.isDiacritic('æ'));
		assertFalse(StringUtils.isDiacritic('a'));
		assertFalse(StringUtils.isDiacritic('z'));
		assertFalse(StringUtils.isDiacritic('Z'));
		assertTrue(StringUtils.isDiacritic('Ø'));
		assertTrue(StringUtils.isDiacritic('ó'));
	}
}
