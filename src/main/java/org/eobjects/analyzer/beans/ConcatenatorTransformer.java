package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Concatenator")
public class ConcatenatorTransformer implements Transformer<String> {

	@Configured
	InputColumn<?>[] columns;

	@Override
	public int getOutputColumns() {
		return 1;
	}

	@Override
	public String[] transform(InputRow inputRow) {
		StringBuilder sb = new StringBuilder();
		for (InputColumn<?> inputColumn : columns) {
			Object value = inputRow.getValue(inputColumn);
			if (value != null) {
				sb.append(value);
			}
		}
		return new String[] { sb.toString() };
	}

}
