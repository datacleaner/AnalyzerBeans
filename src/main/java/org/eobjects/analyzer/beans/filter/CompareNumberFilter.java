package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("Compare number")
@Description("Filter rows where a number values is above, below or equal to a threshold value.")
public class CompareNumberFilter implements Filter<CompareCategory> {

	@Configured
	Double threshold;

	@Configured
	InputColumn<Number> input;

	public CompareNumberFilter(Number threshold) {
		this();
		this.threshold = threshold.doubleValue();
	}

	public CompareNumberFilter() {
	}

	@Override
	public CompareCategory categorize(InputRow inputRow) {
		Number value = inputRow.getValue(input);
		return filter(value);
	}

	protected CompareCategory filter(Number value) {
		if (value == null) {
			// TODO: Consider a "not comparable" category?
			return CompareCategory.LOWER;
		}

		if (threshold.equals(value.doubleValue())) {
			return CompareCategory.EQUAL;
		}
		if (threshold.doubleValue() > value.doubleValue()) {
			return CompareCategory.LOWER;
		}
		return CompareCategory.HIGHER;
	}

}
