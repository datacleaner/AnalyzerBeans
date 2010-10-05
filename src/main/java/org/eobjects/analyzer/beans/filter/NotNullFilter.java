package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("Not null")
public class NotNullFilter implements Filter<ValidationCategory> {

	@Configured
	InputColumn<?> input;

	@Override
	public ValidationCategory categorize(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		if (value == null) {
			return ValidationCategory.INVALID;
		}
		return ValidationCategory.VALID;
	}
}
