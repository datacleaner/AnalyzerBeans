package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.VirtualStringInputColumn;

@TransformerBean("Concatenator")
public class ConcatenatorTransformer implements Transformer<String> {

	@Configured
	InputColumn<?>[] columns;

	@Configured("Output name")
	String outputName;

	@SuppressWarnings("unchecked")
	@Override
	public InputColumn<String>[] getVirtualInputColumns() {
		return new InputColumn[] { new VirtualStringInputColumn(outputName) };
	}

	@Override
	public String[] transform(InputRow inputRow) {
		StringBuilder sb = new StringBuilder();
		for (InputColumn<?> inputColumn : columns) {
			Object value = inputRow.getValue(inputColumn);
			if (value != null) {
				sb.append(value);
			}
		}
		return new String[] { sb.toString() };
	}

}
