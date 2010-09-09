package org.eobjects.analyzer.util;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class ReflectionUtils {

	private ReflectionUtils() {
		// Prevent instantiation
	}

	public static boolean is(Type thisType, Class<?> ofThatType) {
		Class<?> thisClass = null;
		if (thisType instanceof Class<?>) {
			thisClass = (Class<?>) thisType;
			if (thisClass.isArray()) {
				thisClass = thisClass.getComponentType();
			}
		}

		if (thisClass == ofThatType) {
			return true;
		}

		if (thisClass.isPrimitive() != ofThatType.isPrimitive()) {
			if (isByte(thisClass) && isByte(ofThatType)) {
				return true;
			}
			if (isCharacter(thisClass) && isCharacter(ofThatType)) {
				return true;
			}
			if (isBoolean(thisClass) && isBoolean(ofThatType)) {
				return true;
			}
			if (isShort(thisClass) && isShort(ofThatType)) {
				return true;
			}
			if (isInteger(thisClass) && isInteger(ofThatType)) {
				return true;
			}
			if (isLong(thisClass) && isLong(ofThatType)) {
				return true;
			}
			if (isFloat(thisClass) && isFloat(ofThatType)) {
				return true;
			}
			if (isDouble(thisClass) && isDouble(ofThatType)) {
				return true;
			}
		}
		return ofThatType.isAssignableFrom(thisClass);
	}

	private static boolean isCharacter(Class<?> type) {
		return (type == char.class || type == Character.class);
	}

	public static boolean isInputColumn(Class<?> type) {
		return is(type, InputColumn.class);
	}

	public static boolean isColumn(Class<?> type) {
		return is(type, Column.class);
	}

	public static boolean isTable(Class<?> type) {
		return is(type, Table.class);
	}

	public static boolean isSchema(Class<?> type) {
		return is(type, Schema.class);
	}

	public static boolean isCloseable(Class<?> type) {
		return is(type, Closeable.class);
	}

	public static boolean isBoolean(Type type) {
		return (type == Boolean.class || type == boolean.class);
	}

	public static boolean isString(Type type) {
		return is(type, String.class);
	}

	public static boolean isShort(Type type) {
		return (type == Short.class || type == short.class);
	}

	public static boolean isDouble(Type type) {
		return (type == Double.class || type == double.class);
	}

	public static boolean isLong(Type type) {
		return (type == Long.class || type == long.class);
	}

	public static boolean isInteger(Type type) {
		return (type == Integer.class || type == int.class);
	}

	public static boolean isFloat(Type type) {
		return (type == Float.class || type == float.class);
	}

	public static boolean isMap(Type type) {
		return type == Map.class;
	}

	public static boolean isList(Type type) {
		return type == List.class;
	}

	public static boolean isDate(Type type) {
		return type == Date.class;
	}

	public static boolean isNumber(Type type) {
		if (type instanceof Class<?>) {
			Class<?> clazz = (Class<?>) type;
			return Number.class.isAssignableFrom(clazz);
		}
		return false;
	}

	public static boolean isByte(Type type) {
		return type == byte.class || type == Byte.class;
	}

	public static boolean isByteArray(Type type) {
		if (type instanceof Class<?>) {
			Class<?> clazz = (Class<?>) type;
			if (clazz == byte[].class || clazz == Byte[].class) {
				return true;
			}
		}
		return false;
	}

	public static String explodeCamelCase(String str, boolean excludeGetOrSet) {
		if (str == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(str.trim());
		if (sb.length() > 1) {
			if (excludeGetOrSet) {
				if (str.startsWith("get") || str.startsWith("set")) {
					sb.delete(0, 3);
				}
			}

			// Special handling for instance variables that have the "_" prefix
			if (sb.charAt(0) == '_') {
				sb.deleteCharAt(0);
			}

			// First character is set to uppercase
			sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

			boolean previousUpperCase = true;

			for (int i = 1; i < sb.length(); i++) {
				char currentChar = sb.charAt(i);
				if (!previousUpperCase) {
					if (Character.isUpperCase(currentChar)) {
						sb.setCharAt(i, Character.toLowerCase(currentChar));
						sb.insert(i, ' ');
						i++;
					}
				} else {
					if (Character.isLowerCase(currentChar)) {
						previousUpperCase = false;
					}
				}
			}
		}
		return sb.toString();
	}

	public static int getTypeParameterCount(Field field) {
		Type genericType = field.getGenericType();
		return getTypeParameterCount(genericType);
	}

	public static int getTypeParameterCount(Type genericType) {
		if (genericType instanceof GenericArrayType) {
			GenericArrayType gaType = (GenericArrayType) genericType;
			genericType = gaType.getGenericComponentType();
		}
		if (genericType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) genericType;
			Type[] typeArguments = pType.getActualTypeArguments();
			return typeArguments.length;
		}
		return 0;
	}

	public static Class<?> getTypeParameter(Field field, int parameterIndex) {
		Type genericType = field.getGenericType();
		return getTypeParameter(genericType, parameterIndex);
	}

	public static Class<?> getTypeParameter(Type genericType, int parameterIndex) {
		if (genericType instanceof GenericArrayType) {
			GenericArrayType gaType = (GenericArrayType) genericType;
			genericType = gaType.getGenericComponentType();
		}
		if (genericType instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) genericType;
			Type[] typeArguments = ptype.getActualTypeArguments();
			if (typeArguments.length > parameterIndex) {
				Type argument = typeArguments[parameterIndex];
				return getSafeClassToUse(argument);
			} else {
				throw new IllegalArgumentException("Only "
						+ typeArguments.length + " parameters available");
			}
		}
		throw new IllegalArgumentException("Field type is not parameterized: "
				+ genericType);
	}

	public static boolean isWildcard(Type type) {
		return type instanceof WildcardType;
	}

	private static Class<?> getSafeClassToUse(Type someType) {
		if (someType instanceof GenericArrayType) {
			GenericArrayType gaType = (GenericArrayType) someType;
			someType = gaType.getGenericComponentType();
		}

		if (someType instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) someType;

			Type[] upperBounds = wildcardType.getUpperBounds();
			if (upperBounds != null && upperBounds.length > 0) {
				return (Class<?>) upperBounds[0];
			}

			Type[] lowerBounds = wildcardType.getLowerBounds();
			if (lowerBounds != null && lowerBounds.length > 0) {
				return (Class<?>) lowerBounds[0];
			}
		} else if (someType instanceof Class) {
			return (Class<?>) someType;
		} else if (someType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) someType;
			return (Class<?>) pType.getRawType();
		}
		throw new UnsupportedOperationException(
				"Parameter type not supported: " + someType);
	}

	public static int getHierarchyDistance(Class<?> subtype, Class<?> supertype) {
		assert subtype != null;
		assert supertype != null;
		
		if (subtype == supertype) {
			return 0;
		}

		if (!ReflectionUtils.is(subtype, supertype)) {
			throw new IllegalArgumentException("Not a valid subtype of "
					+ supertype.getName() + ": " + subtype.getName());
		}

		if (supertype.isInterface()) {
			Class<?>[] interfaces = subtype.getInterfaces();
			for (Class<?> i : interfaces) {
				if (i == supertype) {
					return 1;
				}
			}
		}
		
		Class<?> subSuperclass = subtype.getSuperclass();
		return 1 + getHierarchyDistance(subSuperclass, supertype);
	}

	public static boolean isArray(Object o) {
		if (o == null) {
			return false;
		}
		return o.getClass().isArray();
	}
}
