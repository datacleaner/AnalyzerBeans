package org.eobjects.analyzer.job;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.util.ReflectionUtils;

@SuppressWarnings("unchecked")
class AbstractBeanJobBuilder<D extends BeanDescriptor, B> {

	private Map<ConfiguredPropertyDescriptor, Object> _properties = new HashMap<ConfiguredPropertyDescriptor, Object>();
	private D _descriptor;

	public AbstractBeanJobBuilder(D descriptor, Class<B> builderClass) {
		_descriptor = descriptor;
		if (!ReflectionUtils.is(getClass(), builderClass)) {
			throw new IllegalArgumentException(
					"Builder class does not correspond to actual class of builder");
		}
	}

	public D getDescriptor() {
		return _descriptor;
	}

	public boolean isConfigured() {
		for (ConfiguredPropertyDescriptor configuredProperty : _descriptor
				.getConfiguredProperties()) {
			if (!getConfiguredProperties().containsKey(configuredProperty)) {
				return false;
			}
		}
		return true;
	}

	public B setConfiguredProperty(String configuredName, Object value) {
		ConfiguredPropertyDescriptor configuredProperty = _descriptor
				.getConfiguredProperty(configuredName);
		if (configuredProperty == null) {
			throw new IllegalArgumentException("No such configured property: "
					+ configuredName);
		}
		return setConfiguredProperty(configuredProperty, value);
	}

	public B setConfiguredProperty(
			ConfiguredPropertyDescriptor configuredProperty, Object value) {
		if (configuredProperty == null) {
			throw new IllegalArgumentException(
					"configuredDescriptor cannot be null");
		}
		if (value != null) {
			boolean correctType = true;
			if (configuredProperty.isArray()) {
				if (value.getClass().isArray()) {
					int length = Array.getLength(value);
					for (int i = 0; i < length; i++) {
						Object valuePart = Array.get(value, i);
						if (valuePart != null) {
							if (!ReflectionUtils.is(valuePart.getClass(),
									configuredProperty.getBaseType())) {
								correctType = false;
							}
						}
					}
				} else {
					if (!ReflectionUtils.is(value.getClass(),
							configuredProperty.getBaseType())) {
						correctType = false;
					}
				}
			} else {
				if (!ReflectionUtils.is(value.getClass(),
						configuredProperty.getBaseType())) {
					correctType = false;
				}
			}
			if (!correctType) {
				throw new IllegalArgumentException("Invalid value type: "
						+ value.getClass().getName() + ", expected: "
						+ configuredProperty.getBaseType().getName());
			}
		}
		_properties.put(configuredProperty, value);
		return (B) this;
	}

	public Map<ConfiguredPropertyDescriptor, Object> getConfiguredProperties() {
		return Collections.unmodifiableMap(_properties);
	}
}
