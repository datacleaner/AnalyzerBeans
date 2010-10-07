package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("Compare number")
public class CompareNumberFilter implements Filter<CompareCategory> {

	@Configured
	Double threshold;

	@Configured
	InputColumn<Number> input;

	@Override
	public CompareCategory categorize(InputRow inputRow) {
		Number value = inputRow.getValue(input);
		if (value == null) {
			// TODO: Consider a "not comparable" category?
			return CompareCategory.LOWER;
		}

		if (threshold.equals(value)) {
			return CompareCategory.EQUAL;
		}
		if (threshold.doubleValue() > value.doubleValue()) {
			return CompareCategory.LOWER;
		}
		return CompareCategory.HIGHER;
	}

}
