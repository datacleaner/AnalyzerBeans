package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.StringUtils;

@TransformerBean("Whitespace trimmer")
@Description("Trims your String values either on left, right or both sides.")
public class WhitespaceTrimmerTransformer implements Transformer<String> {

	@Configured
	InputColumn<String> column;

	@Configured
	boolean trimLeft = true;

	@Configured
	boolean trimRight = true;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(column.getName() + " (trimmed)");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		if (value != null) {
			if (trimLeft && trimRight) {
				value = value.trim();
			} else {
				if (trimLeft) {
					value = StringUtils.leftTrim(value);
				}
				if (trimRight) {
					value = StringUtils.rightTrim(value);
				}
			}
		}
		return new String[] { value };
	}

}
