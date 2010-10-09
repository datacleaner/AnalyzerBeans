package org.eobjects.analyzer.beans.filter;

import junit.framework.TestCase;

public class SingleWordFilterTest extends TestCase {

	public void testFilter() throws Exception {
		SingleWordFilter filter = new SingleWordFilter();
		assertEquals(ValidationCategory.INVALID, filter.filter("hello world"));
		assertEquals(ValidationCategory.VALID, filter.filter("hello"));
		assertEquals(ValidationCategory.INVALID, filter.filter(""));
		assertEquals(ValidationCategory.INVALID, filter.filter(null));
		assertEquals(ValidationCategory.INVALID, filter.filter("hello_world"));
	}
}
