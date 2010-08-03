package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.annotations.Provided;

public class ProvidedPropertyDescriptorImplTest extends TestCase {

	class SampleClass {
		@Provided
		Map<String, Boolean> stringMap;

		@Provided
		void setIntMap(Map<String, Integer> map) {

		}
	}

	public void testGenericTypes() throws Exception {
		Field stringMapField = SampleClass.class.getDeclaredField("stringMap");
		ProvidedPropertyDescriptorImpl descriptor = new ProvidedPropertyDescriptorImpl(stringMapField);

		assertEquals(
				"ProvidedPropertyDescriptorImpl[field=stringMap,baseType=interface java.util.Map]",
				descriptor.toString());

		Method method = SampleClass.class.getDeclaredMethod("setIntMap",
				Map.class);
		descriptor = new ProvidedPropertyDescriptorImpl(method);
		assertEquals(
				"ProvidedPropertyDescriptorImpl[method=setIntMap,baseType=interface java.util.Map]",
				descriptor.toString());
	}
}
