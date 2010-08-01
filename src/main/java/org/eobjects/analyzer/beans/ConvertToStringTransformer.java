package org.eobjects.analyzer.beans;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Convert to string")
public class ConvertToStringTransformer implements Transformer<String> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public String[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		String stringValue = null;
		if (value != null) {
			stringValue = value.toString();
		}
		return new String[] { stringValue };
	}

}
