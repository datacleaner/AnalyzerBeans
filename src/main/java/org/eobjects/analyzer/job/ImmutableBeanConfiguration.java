package org.eobjects.analyzer.job;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.PropertyDescriptor;

final class ImmutableBeanConfiguration implements BeanConfiguration {

	private final Map<PropertyDescriptor, Object> _properties;

	public ImmutableBeanConfiguration(
			Map<ConfiguredPropertyDescriptor, Object> properties) {
		if (properties == null) {
			_properties = new HashMap<PropertyDescriptor, Object>();
		} else {
			_properties = new HashMap<PropertyDescriptor, Object>(properties);
		}

		// validate contents
		for (Map.Entry<PropertyDescriptor, Object> entry : _properties
				.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Collection) {
				throw new IllegalArgumentException(
						"Collection values are not allowed in BeanConfigurations. Violating entry: "
								+ entry.getKey() + " -> " + entry.getValue());
			}
		}
	}

	@Override
	public Object getProperty(ConfiguredPropertyDescriptor propertyDescriptor) {
		return _properties.get(propertyDescriptor);
	}

	@Override
	public String toString() {
		return "ImmutableBeanConfiguration[" + _properties + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_properties == null) ? 0 : _properties.keySet().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImmutableBeanConfiguration other = (ImmutableBeanConfiguration) obj;
		Set<PropertyDescriptor> keySet = _properties.keySet();
		if (other._properties.keySet().equals(keySet)) {
			for (PropertyDescriptor key : keySet) {
				Object v1 = _properties.get(key);
				Object v2 = other._properties.get(key);
				if (v1 != v2 && !v1.equals(v2)) {
					if (v1.getClass().isArray() && v2.getClass().isArray()) {
						if (!Arrays.equals((Object[]) v1, (Object[]) v2)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
}
