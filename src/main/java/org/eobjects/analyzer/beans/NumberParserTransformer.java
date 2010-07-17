package org.eobjects.analyzer.beans;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Number parser")
public class NumberParserTransformer implements Transformer<Double> {

	@Inject
	@Configured
	InputColumn<String> input;

	@Override
	public int getOutputColumns() {
		return 1;
	}

	@Override
	public Double[] transform(InputRow inputRow) {
		String value = inputRow.getValue(input);
		Double d = null;
		if (value != null) {
			try {
				d = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return new Double[] { d };
	}

}
