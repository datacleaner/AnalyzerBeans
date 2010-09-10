package org.eobjects.analyzer.result;

import java.io.Serializable;

import junit.framework.TestCase;

public class CrosstabTest extends TestCase {

	public void testCastValueClass() throws Exception {
		Crosstab<String> c1 = new Crosstab<String>(String.class, "foo", "bar");
		c1.where("foo", "a").where("bar", "b").put("yes", true);

		Crosstab<Serializable> c2 = c1.castValueClass(Serializable.class);
		try {
			c2.where("foo", "a").where("bar", "b").put(3l);
			fail("Excepted exception");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Cannot put value [3] of type [class java.lang.Long] when Crosstab.valueClass is [class java.lang.String]",
					e.getMessage());
		}

		try {
			c2.castValueClass(Number.class);
			fail("Excepted exception");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Unable to cast [class java.lang.String] to [class java.lang.Number]",
					e.getMessage());
		}

	}
}
