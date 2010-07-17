package org.eobjects.analyzer.beans;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("String converter")
public class StringConverterTransformer implements Transformer<String> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Override
	public int getOutputColumns() {
		return 1;
	}

	@Override
	public String[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		String stringValue = null;
		if (value != null) {
			stringValue = value.toString();
		}
		return new String[] { stringValue };
	}

}
