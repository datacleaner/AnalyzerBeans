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
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.Dictionary;

@TransformerBean("Dictionary matcher")
@Description("Matches string values against a set of dictionaries, producing a corresponding set of output columns specifying whether or not the values exist in those dictionaries")
public class DictionaryMatcherTransformer implements Transformer<Boolean> {

	@Configured
	Dictionary[] _dictionaries;

	@Configured
	InputColumn<?> _column;

	public DictionaryMatcherTransformer() {
	}

	public DictionaryMatcherTransformer(InputColumn<?> column, Dictionary[] dictionaries) {
		this();
		_column = column;
		_dictionaries = dictionaries;
	}
	
	public void setDictionaries(Dictionary[] dictionaries) {
		_dictionaries = dictionaries;
	}
	
	public void setColumn(InputColumn<?> column) {
		_column = column;
	}

	@Override
	public OutputColumns getOutputColumns() {
		String columnName = _column.getName();
		String[] names = new String[_dictionaries.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = columnName + " in '" + _dictionaries[i].getName() + "'";
		}
		return new OutputColumns(names);
	}

	@Override
	public Boolean[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(_column);
		return transform(value);
	}

	public Boolean[] transform(final Object value) {
		String stringValue = ConvertToStringTransformer.transformValue(value);
		Boolean[] result = new Boolean[_dictionaries.length];
		if (stringValue != null) {
			for (int i = 0; i < result.length; i++) {
				boolean containsValue = _dictionaries[i].containsValue(stringValue);
				result[i] = containsValue;
			}
		}
		return result;
	}

}
