package org.eobjects.analyzer.util;

import java.util.Arrays;

import junit.framework.TestCase;

public class CollectionUtilsTest extends TestCase {

	public void testArray1() throws Exception {
		String[] result = CollectionUtils.array(new String[] { "foo", "bar" },
				"hello", "world");
		assertEquals("[foo, bar, hello, world]", Arrays.toString(result));
	}

	public void testArray2() throws Exception {
		Object existingArray = new Object[] { 'c' };
		Object[] result = CollectionUtils.array(Object.class, existingArray, "foo", 1, "bar");

		assertEquals("[c, foo, 1, bar]", Arrays.toString(result));
	}
}
