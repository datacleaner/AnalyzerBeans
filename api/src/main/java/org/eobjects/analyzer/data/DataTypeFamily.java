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
package org.eobjects.analyzer.data;

import java.lang.reflect.Type;
import java.util.Date;

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
 * 
 * @deprecated {@link DataTypeFamily} is no longer sufficient for AnalyzerBeans'
 *             scope. The full Java type system will be used.
 */
@Deprecated
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
		if (javaDataType instanceof Class) {
			Class<?> cls = (Class<?>) javaDataType;
			if (String.class.isAssignableFrom(cls)) {
				return STRING;
			}
			if (Boolean.class.isAssignableFrom(cls) || boolean.class.isAssignableFrom(cls)) {
				return BOOLEAN;
			}
			if (Number.class.isAssignableFrom(cls)) {
				return NUMBER;
			}
			if (Date.class.isAssignableFrom(cls)) {
				return DATE;
			}
		}
		return UNDEFINED;
	}
}
