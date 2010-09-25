package org.eobjects.analyzer.beans;

import java.util.Date;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Attempts to convert anything to a Number (Double) value
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to number")
public class ConvertToNumberTransformer implements Transformer<Number> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		Number n = transformValue(value);
		return new Number[] { n };
	}

	public static Number transformValue(Object value) {
		Number n = null;
		if (value != null) {
			if (value instanceof Number) {
				n = (Number) value;
			} else if (value instanceof Boolean) {
				if (Boolean.TRUE.equals(value)) {
					n = 1;
				} else {
					n = 0;
				}
			} else if (value instanceof Date) {
				Date d = (Date) value;
				n = d.getTime();
			} else {
				String stringValue = value.toString();
				try {
					n = Double.parseDouble(stringValue);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		return n;
	}

	public void setInput(InputColumn<String> input) {
		this.input = input;
	}
}
