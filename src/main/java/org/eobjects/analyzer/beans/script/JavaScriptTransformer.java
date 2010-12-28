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

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.StringProperty;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transformer that uses userwritten JavaScript to generate a value
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("JavaScript transformer")
@Description("Supply your own piece of JavaScript to do a custom transformation")
public class JavaScriptTransformer implements Transformer<String> {

	private static final Logger logger = LoggerFactory.getLogger(JavaScriptTransformer.class);

	@Configured
	InputColumn<?>[] columns;

	@Configured
	@Description("Available variables:\nvalues[0..]: Array of values\nvalues[\"my_col\"]: Map of values\nmy_col: Each column value has it's own variable\nout: Print to console using out.println('hello')\nlogger: Print to log using log.info(...), log.warn(...), log.error(...)")
	@StringProperty(multiline = true, mimeType = { "text/javascript", "application/x-javascript" })
	String sourceCode = "function eval() {\n  return \"hello \" + values[0];\n}\n\neval();";

	private ContextFactory _contextFactory;
	private Script _script;

	// this scope is shared between all threads
	private ScriptableObject _sharedScope;

	@Initialize
	public void init() {
		_contextFactory = new ContextFactory();
		Context context = _contextFactory.enterContext();

		try {
			_script = context.compileString(sourceCode, this.getClass().getSimpleName(), 1, null);
			_sharedScope = context.initStandardObjects();

			addToScope(_sharedScope, logger, "logger", "log");
			addToScope(_sharedScope, System.out, "out");
		} finally {
			Context.exit();
		}
	}

	private void addToScope(Scriptable scope, Object object, String... names) {
		Object jsObject = Context.javaToJS(object, scope);
		for (String name : names) {
			name = name.replaceAll(" ", "_");
			ScriptableObject.putProperty(scope, name, jsObject);
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("JavaScript output");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		Context context = _contextFactory.enterContext();

		try {

			// this scope is local to the execution of a single row
			Scriptable scope = context.newObject(_sharedScope);
			scope.setPrototype(_sharedScope);
			scope.setParentScope(null);

			NativeArray values = new NativeArray(columns.length * 2);
			for (int i = 0; i < columns.length; i++) {
				InputColumn<?> column = columns[i];
				Object value = inputRow.getValue(column);

				if (column.getDataTypeFamily() == DataTypeFamily.NUMBER) {
					value = Context.toNumber(value);
				} else if (column.getDataTypeFamily() == DataTypeFamily.BOOLEAN) {
					value = Context.toBoolean(value);
				}

				values.put(i, values, value);
				values.put(column.getName(), values, value);

				addToScope(scope, value, column.getName(), column.getName().toLowerCase(), column.getName().toUpperCase());
			}

			addToScope(scope, values, "values");

			Object result = _script.exec(context, scope);
			String stringResult = Context.toString(result);

			return new String[] { stringResult };
		} finally {
			Context.exit();
		}
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public void setColumns(InputColumn<?>[] columns) {
		this.columns = columns;
	}
}
