package org.eobjects.analyzer.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eobjects.analyzer.annotations.Require;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class RequireDescriptor {

	private String _name;
	private boolean _array;
	private Class<?> _baseType;
	private Field _field;
	private Method _method;

	public RequireDescriptor(Field field, Require requireAnnotation)
			throws IllegalArgumentException {
		_name = requireAnnotation.value();
		_field = field;
		setType(field.getType());
	}

	public RequireDescriptor(Method method, Require requireAnnotation)
			throws IllegalArgumentException {
		_name = requireAnnotation.value();
		_method = method;
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new IllegalArgumentException("The @Require annotated method "
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

	public boolean isBoolean() {
		return (_baseType == Boolean.class || _baseType == boolean.class);
	}

	public boolean isString() {
		return String.class == _baseType;
	}

	public boolean isColumn() {
		return Column.class.isAssignableFrom(_baseType);
	}

	public boolean isTable() {
		return Table.class.isAssignableFrom(_baseType);
	}

	private void setBaseType(Class<?> type) {
		_baseType = type;
		if (!(isBoolean() || isInteger() || isLong() || isDouble()
				|| isString() || isColumn() || isTable())) {
			throw new IllegalArgumentException("The type " + _baseType
					+ " is not supported by the @Require annotation");
		}
	}

	public boolean isDouble() {
		return (_baseType == Double.class || _baseType == double.class);
	}

	public boolean isLong() {
		return (_baseType == Long.class || _baseType == long.class);
	}

	public boolean isInteger() {
		return (_baseType == Integer.class || _baseType == int.class);
	}

	public void assignValue(Object analyser, Object value)
			throws IllegalArgumentException {
		try {
			if (_method != null) {
				_method.invoke(analyser, value);
			} else {
				_field.set(analyser, value);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not assign value '"
					+ value + "' to " + (_method == null ? _field : _method), e);
		}
	}

	@Override
	public String toString() {
		return "RequireDescriptor[method=" + _method + ",field=" + _field + "]";
	}
}
