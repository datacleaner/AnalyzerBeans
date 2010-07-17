package org.eobjects.analyzer.data;

import org.eobjects.analyzer.descriptors.AnnotationHelper;

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

	public static DataTypeFamily valueOf(Class<?> javaDataType) {
		if (AnnotationHelper.isString(javaDataType)) {
			return STRING;
		}
		if (AnnotationHelper.isBoolean(javaDataType)) {
			return BOOLEAN;
		}
		if (AnnotationHelper.isNumber(javaDataType)) {
			return NUMBER;
		}
		if (AnnotationHelper.isDate(javaDataType)) {
			return DATE;
		}
		return UNDEFINED;
	}
}
