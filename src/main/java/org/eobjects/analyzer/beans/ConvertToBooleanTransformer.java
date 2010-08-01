package org.eobjects.analyzer.beans;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Convert to boolean")
public class ConvertToBooleanTransformer implements Transformer<Boolean> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public Boolean[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		Boolean b = null;
		if (value != null) {
			if (value instanceof String) {
				String stringValue = (String) value;
				stringValue = stringValue.trim();
				if ("true".equalsIgnoreCase(stringValue)
						|| "yes".equalsIgnoreCase(stringValue)
						|| "1".equalsIgnoreCase(stringValue)) {
					b = true;
				} else if ("false".equalsIgnoreCase(stringValue)
						|| "no".equalsIgnoreCase(stringValue)
						|| "0".equalsIgnoreCase(stringValue)) {
					b = false;
				}
			} else if (value instanceof Number) {
				Number numberValue = (Number) value;
				if (numberValue.intValue() == 1) {
					b = true;
				} else if (numberValue.intValue() == 0) {
					b = false;
				}
			} else if (value instanceof Boolean) {
				b = (Boolean) value;
			}
		}
		return new Boolean[] { b };
	}

}
