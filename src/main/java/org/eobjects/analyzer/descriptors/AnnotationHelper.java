package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class AnnotationHelper {

	private AnnotationHelper() {
		// Prevent instantiation
	}

	public static boolean isColumn(Class<?> type) {
		return Column.class.isAssignableFrom(type);
	}

	public static boolean isTable(Class<?> type) {
		return Table.class.isAssignableFrom(type);
	}

	public static boolean isSchema(Class<?> type) {
		return Schema.class.isAssignableFrom(type);
	}

	public static boolean isCloseable(Class<?> type) {
		return Cloneable.class.isAssignableFrom(type);
	}

	public static boolean isBoolean(Type type) {
		return (type == Boolean.class || type == boolean.class);
	}

	public static boolean isString(Type type) {
		return String.class == type;
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

	public static boolean isMap(Type type) {
		return type == Map.class;
	}

	public static boolean isList(Type type) {
		return type == List.class;
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

	public static String explodeCamelCase(String str) {
		if (str == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(str.trim());
		if (sb.length() > 1) {

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
}
