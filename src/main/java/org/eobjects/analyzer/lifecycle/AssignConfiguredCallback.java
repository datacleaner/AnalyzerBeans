package org.eobjects.analyzer.lifecycle;

import java.lang.reflect.Array;
import java.util.List;

import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssignConfiguredCallback implements LifeCycleCallback {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private BeanConfiguration _beanConfiguration;

	public AssignConfiguredCallback(BeanConfiguration beanConfiguration) {
		_beanConfiguration = beanConfiguration;
	}

	@Override
	public void onEvent(LifeCycleState state, Object bean,
			AbstractBeanDescriptor descriptor) {
		assert state == LifeCycleState.ASSIGN_CONFIGURED;

		List<ConfiguredDescriptor> configuredDescriptors = descriptor
				.getConfiguredDescriptors();
		for (ConfiguredDescriptor configuredDescriptor : configuredDescriptors) {
			Object configuredValue = getConfiguredValue(configuredDescriptor);
			if (configuredValue == null) {
				configuredDescriptor.assignValue(bean, null);
			} else {
				if (configuredDescriptor.isArray()) {
					configuredDescriptor.assignValue(bean, configuredValue);
				} else {
					if (configuredValue.getClass().isArray()) {
						if (Array.getLength(configuredValue) > 0) {
							configuredValue = Array.get(configuredValue, 0);
						} else {
							configuredValue = null;
						}
					}
					configuredDescriptor.assignValue(bean, configuredValue);
				}
			}
		}
	}

	protected Object getConfiguredValue(
			ConfiguredDescriptor configuredDescriptor) {
		logger.debug("Getting property from bean configuration");
		Object value = _beanConfiguration.getProperty(configuredDescriptor);
		logger.debug("{} -> {}", configuredDescriptor.getName(), value);
		return value;
	}
}
