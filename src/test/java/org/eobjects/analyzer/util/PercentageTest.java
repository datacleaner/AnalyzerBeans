package org.eobjects.analyzer.util;

import junit.framework.TestCase;

public class PercentageTest extends TestCase {

	public void testParsePercentage() throws Exception {
		Percentage p;
		p = Percentage.parsePercentage("95%");
		assertEquals(0.95, p.doubleValue());
		p = Percentage.parsePercentage("0%");
		assertEquals(0.0, p.doubleValue());
		p = Percentage.parsePercentage("100%");
		assertEquals(1.0, p.doubleValue());
		p = Percentage.parsePercentage("4%");
		assertEquals(0.04, p.doubleValue());

		try {
			Percentage.parsePercentage("4");
			fail("Exception expected");
		} catch (NumberFormatException e) {
			assertEquals("4", e.getMessage());
		}
		
		try {
			Percentage.parsePercentage(null);
			fail("Exception expected");
		} catch (NumberFormatException e) {
			assertEquals("cannot parse null", e.getMessage());
		}
		
		try {
			Percentage.parsePercentage("4 %");
			fail("Exception expected");
		} catch (NumberFormatException e) {
			assertEquals("For input string: \"4 \"", e.getMessage());
		}
	}
}
