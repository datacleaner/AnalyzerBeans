package org.eobjects.analyzer.beans.convert;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Attempts to convert anything to a Boolean value
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to boolean")
@Description("Converts anything to a boolean (or null).")
public class ConvertToBooleanTransformer implements Transformer<Boolean> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Configured(required = false)
	Boolean nullReplacement;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(input.getName() + " (as boolean)");
	}

	@Override
	public Boolean[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		Boolean b = transformValue(value);
		if (b == null) {
			b = nullReplacement;
		}
		return new Boolean[] { b };
	}

	public static Boolean transformValue(Object value) {
		Boolean b = null;
		if (value != null) {
			if (value instanceof String) {
				String stringValue = (String) value;
				stringValue = stringValue.trim();
				if ("true".equalsIgnoreCase(stringValue) || "yes".equalsIgnoreCase(stringValue)
						|| "1".equalsIgnoreCase(stringValue)) {
					b = true;
				} else if ("false".equalsIgnoreCase(stringValue) || "no".equalsIgnoreCase(stringValue)
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
		return b;
	}

}
