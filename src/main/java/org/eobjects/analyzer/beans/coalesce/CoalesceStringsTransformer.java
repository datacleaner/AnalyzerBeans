package org.eobjects.analyzer.beans.coalesce;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Coalesce strings")
@Description("Returns the first non-null string.")
public class CoalesceStringsTransformer implements Transformer<String> {

	@Configured
	InputColumn<String>[] input;

	public CoalesceStringsTransformer() {
	}

	public CoalesceStringsTransformer(InputColumn<String>... input) {
		this();
		this.input = input;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Coalsced string");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		for (InputColumn<String> column : input) {
			String value = inputRow.getValue(column);
			if (value != null) {
				return new String[] { value };
			}
		}
		return new String[1];
	}

}
