package org.eobjects.analyzer.job;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;

import junit.framework.TestCase;

public class ImmutableBeanConfigurationTest extends TestCase {

	@Configured
	String[] conf;

	public void testEqualsArrayValue() throws Exception {
		Field field = getClass().getDeclaredField("conf");
		ConfiguredDescriptor descriptor = new ConfiguredDescriptor(field,
				field.getAnnotation(Configured.class));

		Map<ConfiguredDescriptor, Object> properties1 = new HashMap<ConfiguredDescriptor, Object>();
		properties1.put(descriptor, new String[] {"hello", "world"});
		
		Map<ConfiguredDescriptor, Object> properties2 = new HashMap<ConfiguredDescriptor, Object>();
		properties2.put(descriptor, new String[] {"hello", "world"});
		
		assertEquals(new ImmutableBeanConfiguration(properties1), new ImmutableBeanConfiguration(properties2));
	}
}
