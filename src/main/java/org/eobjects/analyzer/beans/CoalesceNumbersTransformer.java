package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Coalesce numbers")
public class CoalesceNumbersTransformer implements Transformer<Number> {

	@Configured
	InputColumn<Number>[] input;

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		for (InputColumn<Number> column : input) {
			Number value = inputRow.getValue(column);
			if (value != null) {
				return new Number[] { value };
			}
		}
		return new Number[1];
	}

}
