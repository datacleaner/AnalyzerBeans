package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

public interface BeanConfiguration {

	public Object getProperty(ConfiguredPropertyDescriptor propertyDescriptor);
}