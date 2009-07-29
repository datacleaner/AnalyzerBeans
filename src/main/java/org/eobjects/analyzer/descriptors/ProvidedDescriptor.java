package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public class ProvidedDescriptor {

	private Field _field;
	private Method _method;
	private Class<?> _baseType;
	private Type[] _typeArguments;

	public ProvidedDescriptor(Field field, Provided providedAnnotation)
			throws DescriptorException {
		_field = field;
		_field.setAccessible(true);
		setType(_field.getType());
		setGenericType(_field.getGenericType());
	}

	public ProvidedDescriptor(Method method, Provided providedAnnotation)
			throws DescriptorException {
		_method = method;
		_method.setAccessible(true);
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new DescriptorException("The @Provided annotated method "
					+ method + " defines " + parameterTypes.length
					+ " parameters, a single parameter is required");
		}
		setType(parameterTypes[0]);
		setGenericType(_method.getGenericParameterTypes()[0]);
	}

	private void setType(Class<?> type) throws DescriptorException {
		if (!(AnnotationHelper.isMap(type) || AnnotationHelper.isList(type) || SchemaNavigator.class != type)) {
			throw new DescriptorException("The type " + _baseType
					+ " is not supported by the @Provided annotation");
		}
		_baseType = type;
	}

	private void setGenericType(Type genericType) throws DescriptorException {
		if (genericType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			_typeArguments = parameterizedType.getActualTypeArguments();
			for (Type type : _typeArguments) {
				if (!(AnnotationHelper.isString(type)
						|| AnnotationHelper.isBoolean(type)
						|| AnnotationHelper.isDouble(type)
						|| AnnotationHelper.isInteger(type) || AnnotationHelper
						.isLong(type))) {
					throw new DescriptorException("The type " + _baseType
							+ " is not supported by the @Provided annotation");
				}
			}
		}
	}

	@Override
	public String toString() {
		if (_field == null) {
			return "ProvidedDescriptor[method=" + _method.getName()
					+ ",baseType=" + _baseType + ",typeParameters="
					+ ArrayUtils.toString(_typeArguments) + "]";
		} else {
			return "ProvidedDescriptor[field=" + _field.getName()
					+ ",baseType=" + _baseType + ",typeParameters="
					+ ArrayUtils.toString(_typeArguments) + "]";
		}
	}

	public void assignValue(Object analyzerBean, Object value)
			throws IllegalStateException {
		try {
			if (_method != null) {
				_method.invoke(analyzerBean, value);
			} else {
				_field.set(analyzerBean, value);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not assign value '" + value
					+ "' to " + (_method == null ? _field : _method), e);
		}
	}

	public boolean isList() {
		return AnnotationHelper.isList(_baseType);
	}

	public boolean isMap() {
		return AnnotationHelper.isMap(_baseType);
	}

	public boolean isSchemaNavigator() {
		return _baseType == SchemaNavigator.class;
	}

	public boolean isDataContext() {
		return _baseType == DataContext.class;
	}

	public int getTypeArgumentCount() {
		return _typeArguments.length;
	}

	public Class<?> getTypeArgument(int i) {
		return (Class<?>) _typeArguments[i];
	}
}
