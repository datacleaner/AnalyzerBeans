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
package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.StringPattern;

@TransformerBean("String pattern matcher")
public class StringPatternMatcherTransformer implements Transformer<Boolean> {

	@Configured
	StringPattern[] _stringPatterns;

	@Configured
	InputColumn<?> _column;

	@Override
	public OutputColumns getOutputColumns() {
		String columnName = _column.getName();
		String[] names = new String[_stringPatterns.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = columnName + " '" + _stringPatterns[i].getName() + "'";
		}
		return new OutputColumns(names);
	}

	@Override
	public Boolean[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(_column);

		Boolean[] result = doMatching(value);

		return result;
	}

	public Boolean[] doMatching(Object value) {
		Boolean[] result = new Boolean[_stringPatterns.length];
		String stringValue = ConvertToStringTransformer.transformValue(value);

		for (int i = 0; i < result.length; i++) {
			result[i] = _stringPatterns[i].matches(stringValue);
		}
		return result;
	}
	
	public void setStringPatterns(StringPattern[] stringPatterns) {
		_stringPatterns = stringPatterns;
	}
	
	public void setColumn(InputColumn<?> column) {
		_column = column;
	}
}