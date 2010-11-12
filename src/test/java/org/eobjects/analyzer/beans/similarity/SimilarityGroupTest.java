package org.eobjects.analyzer.beans.similarity;

import junit.framework.TestCase;

public class SimilarityGroupTest extends TestCase {

	public void testEqualsAndHashCode() throws Exception {
		SimilarityGroup sv1 = new SimilarityGroup("hello", "world");
		SimilarityGroup sv2 = new SimilarityGroup("hello", "world");
		assertEquals(sv1, sv2);
		assertEquals(sv1.hashCode(), sv2.hashCode());

		sv2 = new SimilarityGroup("world", "hello");
		assertEquals(sv1, sv2);
		assertEquals(sv1.hashCode(), sv2.hashCode());

		assertEquals("SimilarValues[hello,world]", sv1.toString());
		assertEquals("SimilarValues[hello,world]", sv2.toString());

		assertEquals(sv1, sv1);
		assertFalse(sv1.equals(null));
		assertFalse(sv1.equals("hello"));
	}

	public void testContains() throws Exception {
		SimilarityGroup sv = new SimilarityGroup("hello", "world");
		assertTrue(sv.contains("hello"));
		assertTrue(sv.contains("world"));
		assertFalse(sv.contains("helloo"));
		assertFalse(sv.contains("ello"));
		assertFalse(sv.contains(null));
	}
}
