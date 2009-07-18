package org.eobjects.analyzer.engine;

import org.eobjects.analyzer.descriptors.AnnotationHelper;

import junit.framework.TestCase;

public class AnnotationHelperTest extends TestCase {

	public void testExplodeCamelCase() throws Exception {
		assertEquals("Foo bar", AnnotationHelper.explodeCamelCase("fooBar"));
		assertEquals("f", AnnotationHelper.explodeCamelCase("f"));
		assertEquals("", AnnotationHelper.explodeCamelCase(""));
		assertEquals("My name is john doe", AnnotationHelper.explodeCamelCase("MyNameIsJohnDoe"));
		assertEquals("H e l l o", AnnotationHelper.explodeCamelCase("h e l l o"));
	}
}
