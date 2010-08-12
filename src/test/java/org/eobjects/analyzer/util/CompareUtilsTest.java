package org.eobjects.analyzer.util;

import junit.framework.TestCase;

public class CompareUtilsTest extends TestCase {

	public void testEquals() throws Exception {
		assertTrue(CompareUtils.equals(null, null));
		assertTrue(CompareUtils.equals("hello", "hello"));
		assertFalse(CompareUtils.equals("hello", null));
		assertFalse(CompareUtils.equals(null, "hello"));
		assertFalse(CompareUtils.equals("world", "hello"));

		MyCloneable o1 = new MyCloneable();
		assertTrue(CompareUtils.equals(o1, o1));
		MyCloneable o2 = o1.clone();
		assertFalse(CompareUtils.equals(o1, o2));
	}

	public void testCompare() throws Exception {
		assertEquals(0, CompareUtils.compare(null, null));
		assertEquals(-1, CompareUtils.compare(null, "hello"));
		assertEquals(1, CompareUtils.compare("hello", null));
		assertEquals(0, CompareUtils.compare("hello", "hello"));
		assertEquals("hello".compareTo("world"),
				CompareUtils.compare("hello", "world"));
	}

	static final class MyCloneable implements Cloneable {
		@Override
		public boolean equals(Object obj) {
			return false;
		}

		@Override
		public MyCloneable clone() {
			try {
				return (MyCloneable) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new UnsupportedOperationException();
			}
		}
	};
}
