package org.eobjects.analyzer.job;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
class AbstractBeanJobBuilder<D extends BeanDescriptor<E>, E, B> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private D _descriptor;
	private E _configurableBean;

	public AbstractBeanJobBuilder(D descriptor, Class<?> builderClass) {
		if (descriptor == null) {
			throw new IllegalArgumentException("descriptor cannot be null");
		}
		if (builderClass == null) {
			throw new IllegalArgumentException("builderClass cannot be null");
		}
		_descriptor = descriptor;
		if (!ReflectionUtils.is(getClass(), builderClass)) {
			throw new IllegalArgumentException(
					"Builder class does not correspond to actual class of builder");
		}
		try {
			_configurableBean = _descriptor.getBeanClass().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not instantiate bean class: "
							+ _descriptor.getBeanClass(), e);
		}
	}

	public D getDescriptor() {
		return _descriptor;
	}

	public E getConfigurableBean() {
		return _configurableBean;
	}

	public boolean isConfigured() {
		for (ConfiguredPropertyDescriptor configuredProperty : _descriptor
				.getConfiguredProperties()) {
			if (configuredProperty.isRequired()) {
				if (!getConfiguredProperties().containsKey(configuredProperty)) {
					logger.debug("Configured property is not set: "
							+ configuredProperty);
					return false;
				}
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

		configuredProperty.setValue(_configurableBean, value);
		return (B) this;
	}

	public Map<ConfiguredPropertyDescriptor, Object> getConfiguredProperties() {
		Map<ConfiguredPropertyDescriptor, Object> map = new HashMap<ConfiguredPropertyDescriptor, Object>();
		Set<ConfiguredPropertyDescriptor> configuredProperties = getDescriptor()
				.getConfiguredProperties();
		for (ConfiguredPropertyDescriptor propertyDescriptor : configuredProperties) {
			Object value = propertyDescriptor.getValue(getConfigurableBean());
			if (value != null) {
				map.put(propertyDescriptor, value);
			}
		}
		return Collections.unmodifiableMap(map);
	}
}
