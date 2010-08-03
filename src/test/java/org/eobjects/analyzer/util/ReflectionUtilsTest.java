package org.eobjects.analyzer.util;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.util.ReflectionUtils;

public class ReflectionUtilsTest extends TestCase {
	
	public InputColumn<String> stringInputColumn;
	
	@SuppressWarnings("rawtypes")
	public InputColumn rawInputColumn;
	
	public InputColumn<?> unspecifiedInputColumn;
	
	public InputColumn<? extends Number> unspecifiedNumberInputColumn;
	
	public InputColumn<String>[] stringInputColumns;
	
	public InputColumn<? super Number>[] unspecifiedNumberSuperclassInputColumns;
	
	public InputColumn<Comparable<String>> stringComparableInputColumn;

	public void testExplodeCamelCase() throws Exception {
		assertEquals("Foo bar", ReflectionUtils.explodeCamelCase("fooBar",
				false));
		assertEquals("f", ReflectionUtils.explodeCamelCase("f", false));
		assertEquals("", ReflectionUtils.explodeCamelCase("", false));
		assertEquals("My name is john doe", ReflectionUtils.explodeCamelCase(
				"MyNameIsJohnDoe", false));
		assertEquals("H e l l o", ReflectionUtils.explodeCamelCase(
				"h e l l o", false));

		assertEquals("Name", ReflectionUtils.explodeCamelCase("getName", true));
	}
	
	public void testInputColumnType() throws Exception {
		Field field = getClass().getField("stringInputColumn");
		assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
		assertEquals(String.class, ReflectionUtils.getTypeParameter(field, 0));

		field = getClass().getField("rawInputColumn");
		assertEquals(0, ReflectionUtils.getTypeParameterCount(field));
		
		field = getClass().getField("unspecifiedNumberInputColumn");
		assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
		assertEquals(Number.class, ReflectionUtils.getTypeParameter(field, 0));
		
		field = getClass().getField("stringInputColumns");
		assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
		assertEquals(String.class, ReflectionUtils.getTypeParameter(field, 0));
		assertTrue(field.getType().isArray());
		
		field = getClass().getField("unspecifiedNumberSuperclassInputColumns");
		assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
		assertEquals(Object.class, ReflectionUtils.getTypeParameter(field, 0));
		assertTrue(field.getType().isArray());
		
		field = getClass().getField("stringComparableInputColumn");
		assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
		assertEquals(Comparable.class, ReflectionUtils.getTypeParameter(field, 0));
	}
	
	public void testIsNumber() throws Exception {
		assertTrue(ReflectionUtils.isNumber(Long.class));
		assertTrue(ReflectionUtils.isNumber(Float.class));
		assertFalse(ReflectionUtils.isNumber(String.class));
		assertFalse(ReflectionUtils.isNumber(Object.class));
	}
}
