package org.eobjects.analyzer.data;

import java.lang.reflect.Type;
import java.util.Date;

import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * An enum with high-level data types. The enum values represent the valid type
 * parameters for InputColumn and hence the valid types that can be used to
 * qualify the data types consumed and produced by transformer and analyzer
 * beans.
 * 
 * The type parameters to enum translation table goes like this:
 * 
 * <ul>
 * <li>InputColumn<java.lang.String> -> STRING</li>
 * <li>InputColumn<java.lang.Boolean> -> BOOLEAN</li>
 * <li>InputColumn<java.lang.Number> -> NUMBER</li>
 * <li>InputColumn<java.util.Date> -> DATE</li>
 * <li>InputColumn<java.lang.Object> -> UNDEFINED</li>
 * <li>InputColumn<*> -> UNDEFINED</li>
 * </ul>
 * 
 * Additionally (for the advanced - not recommended), there is support for
 * bounded wildcards:
 * 
 * <ul>
 * <li>InputColumn<* extends (one of the above)> -> (one of the above)</li>
 * <li>InputColumn<* super (anything)> -> UNDEFINED</li>
 * </ul>
 * 
 * @see InputColumn
 * 
 * @author Kasper Sørensen
 */
public enum DataTypeFamily {

	UNDEFINED, STRING, BOOLEAN, NUMBER, DATE;

	public Class<?> getJavaType() {
		switch (this) {
		case STRING:
			return String.class;
		case BOOLEAN:
			return Boolean.class;
		case NUMBER:
			return Number.class;
		case DATE:
			return Date.class;
		default:
			return Object.class;
		}
	}

	public static DataTypeFamily valueOf(Type javaDataType) {
		if (ReflectionUtils.isString(javaDataType)) {
			return STRING;
		}
		if (ReflectionUtils.isBoolean(javaDataType)) {
			return BOOLEAN;
		}
		if (ReflectionUtils.isNumber(javaDataType)) {
			return NUMBER;
		}
		if (ReflectionUtils.isDate(javaDataType)) {
			return DATE;
		}
		return UNDEFINED;
	}
}
