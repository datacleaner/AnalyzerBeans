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
package org.eobjects.analyzer.beans.script;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Various utility methods for dealing with JavaScript (Mozilla Rhino)
 * 
 * @author Kasper Sørensen
 */
final class JavaScriptUtils {

	private JavaScriptUtils() {
		// prevent instantiation
	}

	/**
	 * Adds an object to the JavaScript scope with a set of variable names
	 * 
	 * @param scope
	 * @param object
	 * @param names
	 */
	public static void addToScope(Scriptable scope, Object object, String... names) {
		Object jsObject = Context.javaToJS(object, scope);
		for (String name : names) {
			name = name.replaceAll(" ", "_");
			ScriptableObject.putProperty(scope, name, jsObject);
		}
	}

	/**
	 * Adds the values of a row to the JavaScript scope
	 * 
	 * @param scope
	 * @param inputRow
	 * @param columns
	 * @param arrayName
	 */
	public static void addToScope(Scriptable scope, InputRow inputRow, InputColumn<?>[] columns, String arrayName) {
		NativeArray values = new NativeArray(columns.length * 2);
		for (int i = 0; i < columns.length; i++) {
			InputColumn<?> column = columns[i];
			Object value = inputRow.getValue(column);

			if (value != null) {
				if (column.getDataTypeFamily() == DataTypeFamily.NUMBER) {
					value = Context.toNumber(value);
				} else if (column.getDataTypeFamily() == DataTypeFamily.BOOLEAN) {
					value = Context.toBoolean(value);
				}
			}

			values.put(i, values, value);
			values.put(column.getName(), values, value);

			addToScope(scope, value, column.getName(), column.getName().toLowerCase(), column.getName().toUpperCase());
		}

		addToScope(scope, values, arrayName);
	}
}
