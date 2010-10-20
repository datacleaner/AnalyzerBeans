package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("String length")
public class StringLengthTransformer implements Transformer<Number> {

	@Configured
	InputColumn<String> column;

	public StringLengthTransformer() {
	}

	public StringLengthTransformer(InputColumn<String> column) {
		this.column = column;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		return transform(value);
	}

	protected Number[] transform(String value) {
		Integer length = null;
		if (value != null) {
			length = value.length();
		}
		return new Number[] { length };
	}

}
