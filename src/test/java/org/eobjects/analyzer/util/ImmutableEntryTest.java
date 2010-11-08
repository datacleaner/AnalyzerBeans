package org.eobjects.analyzer.util;

import junit.framework.TestCase;

public class ImmutableEntryTest extends TestCase {

	public void testEntryMethods() throws Exception {
		final ImmutableEntry<String, Integer> e1 = new ImmutableEntry<String, Integer>("hi", 45);
		assertEquals("hi", e1.getKey());
		assertEquals(45, e1.getValue().intValue());

		try {
			e1.setValue(55);
			fail("Exception expected");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		final ImmutableEntry<String, Integer> e2 = new ImmutableEntry<String, Integer>("hi", 45);
		assertEquals(e1.hashCode(), e2.hashCode());
		assertEquals(e1.toString(), e2.toString());
		assertEquals(e1, e2);
	}
}
