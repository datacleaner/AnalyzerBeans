package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;

public interface BeanConfiguration {

	public Object getProperty(ConfiguredDescriptor configuredDescriptor);
}