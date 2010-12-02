package org.eobjects.analyzer.beans.filter;

import junit.framework.TestCase;

public class StringValueRangeFilterTest extends TestCase {

	public void testCategorize() throws Exception {
		StringValueRangeFilter f = new StringValueRangeFilter("AAA", "ccc");
		f.init();

		assertEquals(RangeFilterCategory.LOWER, f.categorize((String) null));
		assertEquals(RangeFilterCategory.LOWER, f.categorize(""));
		assertEquals(RangeFilterCategory.VALID, f.categorize("XXX"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("BBB"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("aaa"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("bbb"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("ccc"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("AAA"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("CCC"));
		assertEquals(RangeFilterCategory.HIGHER, f.categorize("ccd"));
		assertEquals(RangeFilterCategory.HIGHER, f.categorize("xxx"));
	}
}
