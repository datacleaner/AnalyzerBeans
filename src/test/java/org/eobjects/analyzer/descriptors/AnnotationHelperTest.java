package org.eobjects.analyzer.descriptors;

import junit.framework.TestCase;

public class AnnotationHelperTest extends TestCase {

	public void testExplodeCamelCase() throws Exception {
		assertEquals("Foo bar", AnnotationHelper.explodeCamelCase("fooBar",
				false));
		assertEquals("f", AnnotationHelper.explodeCamelCase("f", false));
		assertEquals("", AnnotationHelper.explodeCamelCase("", false));
		assertEquals("My name is john doe", AnnotationHelper.explodeCamelCase(
				"MyNameIsJohnDoe", false));
		assertEquals("H e l l o", AnnotationHelper.explodeCamelCase(
				"h e l l o", false));

		assertEquals("Name", AnnotationHelper.explodeCamelCase("getName", true));
	}
}
