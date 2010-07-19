package org.eobjects.analyzer.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;
import org.eobjects.analyzer.descriptors.AnnotationHelper;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;

class AbstractBeanJobBuilder<E extends AbstractBeanDescriptor> {

	private Map<ConfiguredDescriptor, Object> _properties = new HashMap<ConfiguredDescriptor, Object>();
	private E _descriptor;

	public AbstractBeanJobBuilder(E descriptor) {
		_descriptor = descriptor;
	}

	public E getDescriptor() {
		return _descriptor;
	}

	public boolean isConfigured() {
		for (ConfiguredDescriptor configuredDescriptor : _descriptor
				.getConfiguredDescriptors()) {
			if (!getConfiguredProperties().containsKey(configuredDescriptor)) {
				return false;
			}
		}
		return true;
	}

	public void setConfiguredProperty(String configuredName, Object value) {
		ConfiguredDescriptor configuredDescriptor = _descriptor
				.getConfiguredDescriptor(configuredName);
		if (configuredDescriptor == null) {
			throw new IllegalArgumentException("No such configured property: "
					+ configuredName);
		}
		setConfiguredProperty(configuredDescriptor, value);
	}

	public void setConfiguredProperty(
			ConfiguredDescriptor configuredDescriptor, Object value) {
		if (configuredDescriptor == null) {
			throw new IllegalArgumentException(
					"configuredDescriptor cannot be null");
		}
		if (value != null) {
			if (!AnnotationHelper.is(value.getClass(),
					configuredDescriptor.getBaseType())) {
				throw new IllegalArgumentException("Invalid value type: "
						+ value.getClass().getName() + ", expected: "
						+ configuredDescriptor.getBaseType().getName());
			}
		}
		_properties.put(configuredDescriptor, value);
	}

	public Map<ConfiguredDescriptor, Object> getConfiguredProperties() {
		return Collections.unmodifiableMap(_properties);
	}
}
