package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.Provided;

public class ProvidedPropertyDescriptorImplTest extends TestCase {

	@Provided
	Map<String, Boolean> stringMap;

	@Provided
	Map<String, Integer> intMap;

	public void testGenericTypes() throws Exception {
		Field stringMapField = getClass().getDeclaredField("stringMap");
		ProvidedPropertyDescriptorImpl descriptor = new ProvidedPropertyDescriptorImpl(
				stringMapField, null);

		assertEquals(
				"ProvidedPropertyDescriptorImpl[field=stringMap,baseType=interface java.util.Map]",
				descriptor.toString());
		
		assertEquals(2, descriptor.getTypeArgumentCount());
		assertEquals(String.class, descriptor.getTypeArgument(0));
		assertEquals(Boolean.class, descriptor.getTypeArgument(1));

		Field intMapField = getClass().getDeclaredField("intMap");
		descriptor = new ProvidedPropertyDescriptorImpl(intMapField, null);
		assertEquals(
				"ProvidedPropertyDescriptorImpl[field=intMap,baseType=interface java.util.Map]",
				descriptor.toString());
		
		assertEquals(2, descriptor.getTypeArgumentCount());
		assertEquals(String.class, descriptor.getTypeArgument(0));
		assertEquals(Integer.class, descriptor.getTypeArgument(1));
	}
}
