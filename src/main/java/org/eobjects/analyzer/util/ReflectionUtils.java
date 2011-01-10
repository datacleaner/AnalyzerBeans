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
package org.eobjects.analyzer.util;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;

import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

public class ReflectionUtils {

	private ReflectionUtils() {
		// Prevent instantiation
	}

	/**
	 * @return true if thisType is a valid type ofThatType, either as a single
	 *         instance or as an array of ofThatType
	 */
	public static boolean is(Type thisType, Class<?> ofThatType) {
		return is(thisType, ofThatType, true);
	}

	public static boolean is(Type thisType, Class<?> ofThatType, boolean includeArray) {
		Class<?> thisClass = null;
		if (thisType instanceof Class<?>) {
			thisClass = (Class<?>) thisType;
			if (includeArray && thisClass.isArray()) {
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

	public static boolean isCharacter(Type type) {
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

	public static boolean isSet(Type type) {
		return type == Set.class;
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
			boolean numberClass = is(clazz, Number.class, false);
			if (numberClass) {
				return true;
			}
			return type == byte.class || type == int.class || type == short.class || type == long.class
					|| type == float.class || type == double.class;
		}
		return false;
	}

	public static boolean isByte(Type type) {
		return type == byte.class || type == Byte.class;
	}

	public static boolean isByteArray(Type type) {
		if (type == byte[].class || type == Byte[].class) {
			return true;
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
				throw new IllegalArgumentException("Only " + typeArguments.length + " parameters available");
			}
		}
		throw new IllegalArgumentException("Field type is not parameterized: " + genericType);
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
		throw new UnsupportedOperationException("Parameter type not supported: " + someType);
	}

	public static int getHierarchyDistance(Class<?> subtype, Class<?> supertype) {
		assert subtype != null;
		assert supertype != null;

		if (subtype == supertype) {
			return 0;
		}

		if (!ReflectionUtils.is(subtype, supertype)) {
			throw new IllegalArgumentException("Not a valid subtype of " + supertype.getName() + ": " + subtype.getName());
		}

		Class<?> subSuperclass = subtype.getSuperclass();
		if (subSuperclass != Object.class) {
			return 1 + getHierarchyDistance(subSuperclass, supertype);
		}

		if (supertype.isInterface()) {
			return getInterfaceHierarchyDistance(subtype, supertype);
		} else {
			return 1 + getHierarchyDistance(subSuperclass, supertype);
		}
	}

	private static int getInterfaceHierarchyDistance(Class<?> subtype, Class<?> supertype) {
		if (subtype == supertype) {
			return 0;
		}

		Class<?>[] interfaces = subtype.getInterfaces();
		for (Class<?> i : interfaces) {
			if (i == supertype) {
				return 1;
			}
		}
		for (Class<?> i : interfaces) {
			Class<?>[] subInterfaces = i.getInterfaces();
			if (subInterfaces != null && subInterfaces.length > 0) {
				for (Class<?> subInterface : subInterfaces) {
					int result = getInterfaceHierarchyDistance(subInterface, supertype);
					if (result != -1) {
						return 1 + result;
					}
				}
			}
		}
		return -1;
	}

	public static boolean isArray(Object o) {
		if (o == null) {
			return false;
		}
		return o.getClass().isArray();
	}

	public static Method[] getMethods(Class<?> clazz, Class<? extends Annotation> withAnnotation) {
		List<Method> result = new ArrayList<Method>();

		Method[] methods = getMethods(clazz);
		for (Method method : methods) {
			if (method.isAnnotationPresent(withAnnotation)) {
				result.add(method);
			}
		}

		return result.toArray(new Method[result.size()]);
	}

	public static Field[] getFields(Class<?> clazz, Class<? extends Annotation> withAnnotation) {
		List<Field> result = new ArrayList<Field>();

		Field[] fields = getFields(clazz);
		for (Field field : fields) {
			if (field.isAnnotationPresent(withAnnotation)) {
				result.add(field);
			}
		}

		return result.toArray(new Field[result.size()]);
	}

	/**
	 * Gets all methods of a class, excluding those from Object
	 * 
	 * @param clazz
	 * @return
	 */
	public static Method[] getMethods(Class<?> clazz) {
		if (clazz == Object.class) {
			return new Method[0];
		}
		Method[] m = clazz.getDeclaredMethods();
		Class<?> superclass = clazz.getSuperclass();
		m = CollectionUtils.array(m, getMethods(superclass));
		return m;
	}

	/**
	 * Gets all fields of a class, including private fields in super-classes.
	 * 
	 * @param clazz
	 * @return
	 */
	public static Field[] getFields(Class<?> clazz) {
		if (clazz == Object.class) {
			return new Field[0];
		}
		Field[] f = clazz.getDeclaredFields();
		Class<?> superclass = clazz.getSuperclass();
		f = CollectionUtils.array(f, getFields(superclass));
		return f;
	}

}
