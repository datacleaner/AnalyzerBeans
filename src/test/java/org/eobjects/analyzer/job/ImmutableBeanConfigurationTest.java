package org.eobjects.analyzer.job;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptorImpl;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

import junit.framework.TestCase;

public class ImmutableBeanConfigurationTest extends TestCase {

	@Configured
	String[] conf;

	public void testEqualsArrayValue() throws Exception {
		Field field = getClass().getDeclaredField("conf");
		ConfiguredPropertyDescriptorImpl descriptor = new ConfiguredPropertyDescriptorImpl(
				field, null);

		Map<ConfiguredPropertyDescriptor, Object> properties1 = new HashMap<ConfiguredPropertyDescriptor, Object>();
		properties1.put(descriptor, new String[] { "hello", "world" });

		Map<ConfiguredPropertyDescriptor, Object> properties2 = new HashMap<ConfiguredPropertyDescriptor, Object>();
		properties2.put(descriptor, new String[] { "hello", "world" });

		assertEquals(new ImmutableBeanConfiguration(properties1),
				new ImmutableBeanConfiguration(properties2));
	}
}
