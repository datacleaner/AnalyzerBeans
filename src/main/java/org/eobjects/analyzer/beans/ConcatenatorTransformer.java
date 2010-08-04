package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Concatenates several values into one String value.
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Concatenator")
public class ConcatenatorTransformer implements Transformer<String> {

	@Configured
	InputColumn<?>[] columns;

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
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
