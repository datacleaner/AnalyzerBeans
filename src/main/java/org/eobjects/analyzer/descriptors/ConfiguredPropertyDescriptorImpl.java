package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class ConfiguredPropertyDescriptorImpl extends AbstractPropertyDescriptor implements
		ConfiguredPropertyDescriptor {

	public ConfiguredPropertyDescriptorImpl(Field field, BeanDescriptor<?> beanDescriptor) throws DescriptorException {
		super(field, beanDescriptor);
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
	
	@Override
	public boolean isRequired() {
		Configured configured = getAnnotation(Configured.class);
		if (configured == null) {
			return true;
		}
		return configured.required();
	}
	
	@Override
	public DataTypeFamily getInputColumnDataTypeFamily() {
		if (isInputColumn()) {
			int count = getTypeArgumentCount();
			if (count == 0) {
				return DataTypeFamily.UNDEFINED;
			}
			Type typeArgument = getTypeArgument(0);
			return DataTypeFamily.valueOf(typeArgument);
		}
		return null;
	}
}
