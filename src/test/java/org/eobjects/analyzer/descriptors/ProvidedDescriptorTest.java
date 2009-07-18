package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.descriptors.ProvidedDescriptor;

import junit.framework.TestCase;

public class ProvidedDescriptorTest extends TestCase {

	class SampleClass {
		@Provided
		Map<String, Boolean> stringMap;

		@Provided
		void setIntMap(Map<String, Integer> map) {

		}
	}

	public void testGenericTypes() throws Exception {
		Field stringMapField = SampleClass.class.getDeclaredField("stringMap");
		ProvidedDescriptor descriptor = new ProvidedDescriptor(stringMapField, stringMapField
				.getAnnotation(Provided.class));

		assertEquals("ProvidedDescriptor[field=stringMap,baseType=interface java.util.Map,"
				+ "typeParameters={class java.lang.String,class java.lang.Boolean}]", descriptor.toString());

		Method method = SampleClass.class.getDeclaredMethod("setIntMap", Map.class);
		descriptor = new ProvidedDescriptor(method, method.getAnnotation(Provided.class));
		assertEquals("ProvidedDescriptor[method=setIntMap,baseType=interface java.util.Map,"
				+ "typeParameters={class java.lang.String,class java.lang.Integer}]", descriptor.toString());
	}
}
