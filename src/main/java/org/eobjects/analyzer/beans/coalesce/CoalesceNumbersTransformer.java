package org.eobjects.analyzer.beans.coalesce;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
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
