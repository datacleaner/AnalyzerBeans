package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Concatenates several values into one String value.
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Concatenator")
@Description("Concatenate several column values into one.")
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
