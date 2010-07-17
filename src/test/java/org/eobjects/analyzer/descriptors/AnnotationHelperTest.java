package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;

public class AnnotationHelperTest extends TestCase {
	
	public InputColumn<String> stringInputColumn;
	
	@SuppressWarnings("rawtypes")
	public InputColumn rawInputColumn;
	
	public InputColumn<?> unspecifiedInputColumn;
	
	public InputColumn<? extends Number> unspecifiedNumberInputColumn;
	
	public InputColumn<String>[] stringInputColumns;
	
	public InputColumn<? super Number>[] unspecifiedNumberSuperclassInputColumns;
	
	public InputColumn<Comparable<String>> stringComparableInputColumn;

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
	
	public void testInputColumnType() throws Exception {
		Field field = getClass().getField("stringInputColumn");
		assertEquals(1, AnnotationHelper.getTypeParameterCount(field));
		assertEquals(String.class, AnnotationHelper.getTypeParameter(field, 0));

		field = getClass().getField("rawInputColumn");
		assertEquals(0, AnnotationHelper.getTypeParameterCount(field));
		
		field = getClass().getField("unspecifiedNumberInputColumn");
		assertEquals(1, AnnotationHelper.getTypeParameterCount(field));
		assertEquals(Number.class, AnnotationHelper.getTypeParameter(field, 0));
		
		field = getClass().getField("stringInputColumns");
		assertEquals(1, AnnotationHelper.getTypeParameterCount(field));
		assertEquals(String.class, AnnotationHelper.getTypeParameter(field, 0));
		assertTrue(field.getType().isArray());
		
		field = getClass().getField("unspecifiedNumberSuperclassInputColumns");
		assertEquals(1, AnnotationHelper.getTypeParameterCount(field));
		assertEquals(Object.class, AnnotationHelper.getTypeParameter(field, 0));
		assertTrue(field.getType().isArray());
		
		field = getClass().getField("stringComparableInputColumn");
		assertEquals(1, AnnotationHelper.getTypeParameterCount(field));
		assertEquals(Comparable.class, AnnotationHelper.getTypeParameter(field, 0));
	}
	
	public void testIsNumber() throws Exception {
		assertTrue(AnnotationHelper.isNumber(Long.class));
		assertTrue(AnnotationHelper.isNumber(Float.class));
		assertFalse(AnnotationHelper.isNumber(String.class));
		assertFalse(AnnotationHelper.isNumber(Object.class));
	}
}
