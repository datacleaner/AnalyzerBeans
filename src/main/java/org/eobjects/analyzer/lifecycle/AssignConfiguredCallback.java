/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.lifecycle;

import java.lang.reflect.Array;
import java.util.Set;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AssignConfiguredCallback implements LifeCycleCallback {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final BeanConfiguration _beanConfiguration;

	public AssignConfiguredCallback(BeanConfiguration beanConfiguration) {
		_beanConfiguration = beanConfiguration;
	}

	@Override
	public void onEvent(LifeCycleState state, Object bean, BeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.ASSIGN_CONFIGURED;

		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
		for (ConfiguredPropertyDescriptor property : configuredProperties) {
			Object configuredValue = getValue(property);
			if (configuredValue == null) {
				property.setValue(bean, null);
			} else {
				if (property.isArray()) {
					property.setValue(bean, configuredValue);
				} else {
					if (configuredValue.getClass().isArray()) {
						if (Array.getLength(configuredValue) == 1) {
							configuredValue = Array.get(configuredValue, 0);
						} else if (Array.getLength(configuredValue) > 1) {
							throw new IllegalStateException("Cannot assign an array-value (" + configuredValue
									+ ") to a non-array property (" + property + ")");
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
