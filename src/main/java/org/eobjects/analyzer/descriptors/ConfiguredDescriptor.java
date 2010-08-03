package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.util.ReflectionUtils;

public class ConfiguredDescriptor {

	private String _name;
	private boolean _array;
	private Class<?> _baseType;
	private Field _field;
	private Method _method;

	public ConfiguredDescriptor(Field field, Configured configuredAnnotation)
			throws DescriptorException {
		_name = configuredAnnotation.value();
		_field = field;
		_field.setAccessible(true);
		if (_name == null || _name.trim().equals("")) {
			// Use the fields name if no name has been set
			_name = ReflectionUtils.explodeCamelCase(_field.getName(), true);
		}
		setType(field.getType());
	}

	public ConfiguredDescriptor(Method method, Configured configuredAnnotation)
			throws DescriptorException {
		_name = configuredAnnotation.value();
		_method = method;
		_method.setAccessible(true);
		if (_name == null || _name.trim().equals("")) {
			// Use the methods name if no name has been set
			_name = _method.getName();
			_name = ReflectionUtils.explodeCamelCase(_name.substring(3), true);
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new DescriptorException("The @Configured annotated method "
					+ method + " defines " + parameterTypes.length
					+ " parameters, a single parameter is required");
		}
		setType(parameterTypes[0]);
	}

	public boolean isArray() {
		return _array;
	}

	public Class<?> getBaseType() {
		return _baseType;
	}

	public Type getGenericType() {
		if (_field != null) {
			return _field.getGenericType();
		} else {
			return _method.getGenericReturnType();
		}
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
		if (!(ReflectionUtils.isBoolean(type)
				|| ReflectionUtils.isInteger(type)
				|| ReflectionUtils.isLong(type)
				|| ReflectionUtils.isDouble(type)
				|| ReflectionUtils.isString(type)
				|| ReflectionUtils.isInputColumn(type)
				|| ReflectionUtils.isColumn(type)
				|| ReflectionUtils.isTable(type) || ReflectionUtils
				.isSchema(type))) {
			throw new DescriptorException("The type " + _baseType
					+ " is not supported by the @Configured annotation");
		}
		_baseType = type;
	}

	public void assignValue(Object bean, Object value)
			throws IllegalStateException {
		try {
			if (_method != null) {
				_method.invoke(bean, value);
			} else {
				_field.set(bean, value);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not assign value '" + value
					+ "' to " + (_method == null ? _field : _method), e);
		}
	}

	@Override
	public String toString() {
		return "ConfiguredDescriptor[method=" + _method + ",field=" + _field
				+ "]";
	}

	public boolean isInputColumn() {
		return ReflectionUtils.isInputColumn(_baseType);
	}

	public boolean isColumn() {
		return ReflectionUtils.isColumn(_baseType);
	}

	public boolean isBoolean() {
		return ReflectionUtils.isBoolean(_baseType);
	}

	public boolean isInteger() {
		return ReflectionUtils.isInteger(_baseType);
	}

	public boolean isLong() {
		return ReflectionUtils.isLong(_baseType);
	}

	public boolean isDouble() {
		return ReflectionUtils.isDouble(_baseType);
	}

	public boolean isString() {
		return ReflectionUtils.isString(_baseType);
	}

	public boolean isTable() {
		return ReflectionUtils.isTable(_baseType);
	}

	public boolean isSchema() {
		return ReflectionUtils.isSchema(_baseType);
	}
}
