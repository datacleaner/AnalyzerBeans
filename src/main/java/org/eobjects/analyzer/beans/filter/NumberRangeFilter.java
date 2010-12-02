package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("Number range")
@Description("A filter that filters out rows a number value is outside a specified range")
public class NumberRangeFilter implements Filter<RangeFilterCategory> {

	@Configured
	InputColumn<Number> column;

	@Configured
	Double lowestValue = 0d;

	@Configured
	Double highestValue = 10d;

	public NumberRangeFilter(double lowestValue, double highestValue) {
		this.lowestValue = lowestValue;
		this.highestValue = highestValue;
	}

	public NumberRangeFilter() {
	}

	@Override
	public RangeFilterCategory categorize(InputRow inputRow) {
		Number value = inputRow.getValue(column);
		return categorize(value);
	}

	protected RangeFilterCategory categorize(Number value) {
		if (value == null) {
			return RangeFilterCategory.LOWER;
		}
		double doubleValue = value.doubleValue();
		if (doubleValue < lowestValue.doubleValue()) {
			return RangeFilterCategory.LOWER;
		}
		if (doubleValue > highestValue.doubleValue()) {
			return RangeFilterCategory.HIGHER;
		}

		return RangeFilterCategory.VALID;
	}

}
