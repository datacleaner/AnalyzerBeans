package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Coalesce strings")
public class CoalesceStringsTransformer implements Transformer<String> {

	@Configured
	InputColumn<String>[] input;

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public String[] transform(InputRow inputRow) {
		for (InputColumn<String> column : input) {
			String value = inputRow.getValue(column);
			if (value != null) {
				return new String[] { value };
			}
		}
		return new String[1];
	}

}
