package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class ConfiguredPropertyDescriptorImpl extends AbstractPropertyDescriptor implements
		ConfiguredPropertyDescriptor {

	public ConfiguredPropertyDescriptorImpl(Field field) throws DescriptorException {
		super(field);
	}

	@Override
	public String getName() {
		Configured configured = getAnnotation(Configured.class);
		if (configured != null) {
			String value = configured.value();
			if (value != null && value.length() > 0) {
				return value;
			}
		}
		return ReflectionUtils.explodeCamelCase(super.getName(), true);
	}

	@Override
	public boolean isInputColumn() {
		Class<?> baseType = getBaseType();
		boolean result = ReflectionUtils.isInputColumn(baseType);
		return result;
	}
}
