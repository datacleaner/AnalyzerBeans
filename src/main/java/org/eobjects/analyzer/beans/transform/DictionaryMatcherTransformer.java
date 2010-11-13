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
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.Dictionary;

@TransformerBean("Dictionary matcher")
public class DictionaryMatcherTransformer implements Transformer<Boolean> {

	@Configured
	Dictionary[] dictionaries;

	@Configured
	InputColumn<String> inputColumn;

	public DictionaryMatcherTransformer() {
	}

	public DictionaryMatcherTransformer(Dictionary[] dictionaries) {
		this();
		this.dictionaries = dictionaries;
	}

	@Override
	public OutputColumns getOutputColumns() {
		String[] columnNames = new String[dictionaries.length];
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = dictionaries[i].getName();
		}
		return new OutputColumns(columnNames);
	}

	@Override
	public Boolean[] transform(InputRow inputRow) {
		String value = inputRow.getValue(inputColumn);
		return transform(value);
	}

	public Boolean[] transform(String value) {
		Boolean[] result = new Boolean[dictionaries.length];
		if (value != null) {
			for (int i = 0; i < result.length; i++) {
				boolean containsValue = dictionaries[i].containsValue(value);
				result[i] = containsValue;
			}
		}
		return result;
	}

}
