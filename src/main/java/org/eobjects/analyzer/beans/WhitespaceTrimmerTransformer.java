package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.StringUtils;

@TransformerBean("Whitespace trimmer")
public class WhitespaceTrimmerTransformer implements Transformer<String> {

	@Configured
	InputColumn<String> column;
	
	@Configured
	boolean trimLeft = true;
	
	@Configured
	boolean trimRight = true;
	
	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
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
		return new String[] {value};
	}

}
