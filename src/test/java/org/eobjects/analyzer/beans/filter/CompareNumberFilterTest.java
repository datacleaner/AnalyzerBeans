package org.eobjects.analyzer.beans.filter;

import junit.framework.TestCase;

public class CompareNumberFilterTest extends TestCase {

	public void testFilter() throws Exception {
		CompareNumberFilter filter = new CompareNumberFilter(18);
		assertEquals(CompareCategory.LOWER, filter.filter(17));
		assertEquals(CompareCategory.HIGHER, filter.filter(19));
		assertEquals(CompareCategory.LOWER, filter.filter(-17));
		assertEquals(CompareCategory.EQUAL, filter.filter(18));
	}
}
