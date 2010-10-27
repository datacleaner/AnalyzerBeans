package org.eobjects.analyzer.beans.coalesce;

import java.util.Date;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Coalesce dates")
@Description("Returns the first non-null date.")
public class CoalesceDatesTransformer implements Transformer<Date> {

	@Configured
	InputColumn<Date>[] input;

	public CoalesceDatesTransformer() {
	}

	public CoalesceDatesTransformer(InputColumn<Date>... input) {
		this();
		this.input = input;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Coalsced date");
	}

	@Override
	public Date[] transform(InputRow inputRow) {
		for (InputColumn<Date> column : input) {
			Date value = inputRow.getValue(column);
			if (value != null) {
				return new Date[] { value };
			}
		}
		return new Date[1];
	}

}
