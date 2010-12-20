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
package org.eobjects.analyzer.job.builder;

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

/**
 * @author Kasper Sørensen
 * 
 * @param <D>
 *            the component descriptor type (eg. AnalyzerBeanDescriptor)
 * @param <E>
 *            the actual component type (eg. RowProcessingAnalyzer)
 * @param <B>
 *            the concrete job builder type (eg.
 *            RowProcessingAnalyzerJobBuilder)
 */
@SuppressWarnings("unchecked")
public class AbstractBeanJobBuilder<D extends BeanDescriptor<E>, E, B> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final D _descriptor;
	private final E _configurableBean;

	public AbstractBeanJobBuilder(D descriptor, Class<?> builderClass) {
		if (descriptor == null) {
			throw new IllegalArgumentException("descriptor cannot be null");
		}
		if (builderClass == null) {
			throw new IllegalArgumentException("builderClass cannot be null");
		}
		_descriptor = descriptor;
		if (!ReflectionUtils.is(getClass(), builderClass)) {
			throw new IllegalArgumentException("Builder class does not correspond to actual class of builder");
		}
		try {
			_configurableBean = _descriptor.getComponentClass().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not instantiate bean class: " + _descriptor.getComponentClass(), e);
		}
	}

	public D getDescriptor() {
		return _descriptor;
	}

	public E getConfigurableBean() {
		return _configurableBean;
	}

	public boolean isConfigured(boolean throwException) throws IllegalStateException {
		for (ConfiguredPropertyDescriptor configuredProperty : _descriptor.getConfiguredProperties()) {
			if (!isConfigured(configuredProperty, throwException)) {
				if (throwException) {
					throw new IllegalStateException("Property is not properly configured: " + configuredProperty);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isConfigured() {
		return isConfigured(false);
	}

	public boolean isConfigured(ConfiguredPropertyDescriptor configuredProperty, boolean throwException) {
		if (configuredProperty.isRequired()) {
			if (!getConfiguredProperties().containsKey(configuredProperty)) {
				if (throwException) {
					throw new IllegalStateException("Configured property is not set: " + configuredProperty);
				} else {
					logger.debug("Configured property is not set: " + configuredProperty);
					return false;
				}
			}
		}
		return true;
	}

	public B setConfiguredProperty(String configuredName, Object value) {
		ConfiguredPropertyDescriptor configuredProperty = _descriptor.getConfiguredProperty(configuredName);
		if (configuredProperty == null) {
			throw new IllegalArgumentException("No such configured property: " + configuredName);
		}
		return setConfiguredProperty(configuredProperty, value);
	}

	public B setConfiguredProperty(ConfiguredPropertyDescriptor configuredProperty, Object value) {
		if (configuredProperty == null) {
			throw new IllegalArgumentException("configuredProperty cannot be null");
		}
		if (value != null) {
			boolean correctType = true;
			if (configuredProperty.isArray()) {
				if (value.getClass().isArray()) {
					int length = Array.getLength(value);
					for (int i = 0; i < length; i++) {
						Object valuePart = Array.get(value, i);
						if (valuePart != null) {
							if (!ReflectionUtils.is(valuePart.getClass(), configuredProperty.getBaseType())) {
								correctType = false;
							}
						}
					}
				} else {
					if (!ReflectionUtils.is(value.getClass(), configuredProperty.getBaseType())) {
						correctType = false;
					}
				}
			} else {
				if (!ReflectionUtils.is(value.getClass(), configuredProperty.getBaseType())) {
					correctType = false;
				}
			}
			if (!correctType) {
				throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName() + ", expected: "
						+ configuredProperty.getBaseType().getName());
			}
		}

		configuredProperty.setValue(_configurableBean, value);
		onConfigurationChanged();
		return (B) this;
	}

	public Map<ConfiguredPropertyDescriptor, Object> getConfiguredProperties() {
		Map<ConfiguredPropertyDescriptor, Object> map = new HashMap<ConfiguredPropertyDescriptor, Object>();
		Set<ConfiguredPropertyDescriptor> configuredProperties = getDescriptor().getConfiguredProperties();
		for (ConfiguredPropertyDescriptor propertyDescriptor : configuredProperties) {
			Object value = propertyDescriptor.getValue(getConfigurableBean());
			if (value != null) {
				map.put(propertyDescriptor, value);
			}
		}
		return Collections.unmodifiableMap(map);
	}

	/**
	 * method that can be used by sub-classes to add callback logic when the
	 * configuration of the bean changes
	 */
	public void onConfigurationChanged() {
	}

	public Object getConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor) {
		return propertyDescriptor.getValue(getConfigurableBean());
	}
}
