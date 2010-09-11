package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
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
				boolean containsValue = dictionaries[i].getValues()
						.containsValue(value);
				result[i] = containsValue;
			}
		}
		return result;
	}

}
