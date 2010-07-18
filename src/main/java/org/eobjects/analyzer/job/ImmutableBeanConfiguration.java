package org.eobjects.analyzer.job;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;

public class ImmutableBeanConfiguration implements BeanConfiguration {

	private Map<ConfiguredDescriptor, Object> _properties;

	public ImmutableBeanConfiguration(
			Map<ConfiguredDescriptor, Object> properties) {
		_properties = new HashMap<ConfiguredDescriptor, Object>(properties);
	}

	@Override
	public Object getProperty(ConfiguredDescriptor configuredDescriptor) {
		return _properties.get(configuredDescriptor);
	}

}
