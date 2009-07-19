package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eobjects.analyzer.annotations.Configured;

public class ConfiguredDescriptor {

	private String _name;
	private boolean _array;
	private Class<?> _baseType;
	private Field _field;
	private Method _method;

	public ConfiguredDescriptor(Field field, Configured configuredAnnotation) throws DescriptorException {
		_name = configuredAnnotation.value();
		_field = field;
		_field.setAccessible(true);
		if (_name == null || _name.trim().equals("")) {
			// Use the fields name if no name has been set
			_name = AnnotationHelper.explodeCamelCase(_field.getName(), true);
		}
		setType(field.getType());
	}

	public ConfiguredDescriptor(Method method, Configured configuredAnnotation) throws DescriptorException {
		_name = configuredAnnotation.value();
		_method = method;
		_method.setAccessible(true);
		if (_name == null || _name.trim().equals("")) {
			// Use the methods name if no name has been set
			_name = _method.getName();
			_name = AnnotationHelper.explodeCamelCase(_name.substring(3), true);
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new DescriptorException("The @Configured annotated method " + method + " defines "
					+ parameterTypes.length + " parameters, a single parameter is required");
		}
		setType(parameterTypes[0]);
	}

	public boolean isArray() {
		return _array;
	}

	public Class<?> getBaseType() {
		return _baseType;
	}

	public String getName() {
		return _name;
	}

	private void setType(Class<?> type) {
		if (type.isArray()) {
			_array = true;
			setBaseType(type.getComponentType());
		} else {
			_array = false;
			setBaseType(type);
		}
	}

	private void setBaseType(Class<?> type) {
		if (!(AnnotationHelper.isBoolean(type) || AnnotationHelper.isInteger(type) || AnnotationHelper.isLong(type)
				|| AnnotationHelper.isDouble(type) || AnnotationHelper.isString(type)
				|| AnnotationHelper.isColumn(type) || AnnotationHelper.isTable(type) || AnnotationHelper.isSchema(type))) {
			throw new DescriptorException("The type " + _baseType
					+ " is not supported by the @Configured annotation");
		}
		_baseType = type;
	}

	public void assignValue(Object analyzerBean, Object value) throws IllegalStateException {
		try {
			if (_method != null) {
				_method.invoke(analyzerBean, value);
			} else {
				_field.set(analyzerBean, value);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not assign value '" + value + "' to "
					+ (_method == null ? _field : _method), e);
		}
	}

	@Override
	public String toString() {
		return "ConfiguredDescriptor[method=" + _method + ",field=" + _field + "]";
	}

	public boolean isColumn() {
		return AnnotationHelper.isColumn(_baseType);
	}

	public boolean isBoolean() {
		return AnnotationHelper.isBoolean(_baseType);
	}

	public boolean isInteger() {
		return AnnotationHelper.isInteger(_baseType);
	}

	public boolean isLong() {
		return AnnotationHelper.isLong(_baseType);
	}

	public boolean isDouble() {
		return AnnotationHelper.isDouble(_baseType);
	}

	public boolean isString() {
		return AnnotationHelper.isString(_baseType);
	}

	public boolean isTable() {
		return AnnotationHelper.isTable(_baseType);
	}

	public boolean isSchema() {
		return AnnotationHelper.isSchema(_baseType);
	}
}
