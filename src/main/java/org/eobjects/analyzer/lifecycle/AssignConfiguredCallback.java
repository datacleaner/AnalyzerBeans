package org.eobjects.analyzer.lifecycle;

import java.lang.reflect.Array;
import java.util.Set;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssignConfiguredCallback implements LifeCycleCallback {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final BeanConfiguration _beanConfiguration;

	public AssignConfiguredCallback(BeanConfiguration beanConfiguration) {
		_beanConfiguration = beanConfiguration;
	}

	@Override
	public void onEvent(LifeCycleState state, Object bean,
			BeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.ASSIGN_CONFIGURED;

		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor
				.getConfiguredProperties();
		for (ConfiguredPropertyDescriptor property : configuredProperties) {
			Object configuredValue = getValue(property);
			if (configuredValue == null) {
				property.setValue(bean, null);
			} else {
				if (property.isArray()) {
					property.setValue(bean, configuredValue);
				} else {
					if (configuredValue.getClass().isArray()) {
						if (Array.getLength(configuredValue) > 0) {
							configuredValue = Array.get(configuredValue, 0);
						} else {
							configuredValue = null;
						}
					}
					property.setValue(bean, configuredValue);
				}
			}
		}
	}

	protected Object getValue(ConfiguredPropertyDescriptor propertyDescriptor) {
		logger.debug("Getting property from bean configuration");
		Object value = _beanConfiguration.getProperty(propertyDescriptor);
		logger.debug("{} -> {}", propertyDescriptor.getName(), value);
		return value;
	}
}
