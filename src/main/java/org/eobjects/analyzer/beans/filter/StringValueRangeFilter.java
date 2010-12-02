package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("String value range")
@Description("A filter that filters out values outside a specified value range")
public class StringValueRangeFilter implements Filter<RangeFilterCategory> {

	@Configured
	@Description("The lowest valid string value, eg. AAA")
	String lowestValue;
	
	@Configured
	@Description("The highest valid string value, eg. xxx")
	String highestValue;
	
	@Configured
	InputColumn<String> column;
	
	public StringValueRangeFilter() {
	}

	public StringValueRangeFilter(String lowestValue, String highestValue) {
		this.lowestValue = lowestValue;
		this.highestValue = highestValue;
	}
	
	@Initialize
	public void init() {
		if (lowestValue.compareTo(highestValue) > 0) {
			throw new IllegalStateException("Lowest value is higher than the highest value");
		}
	}

	@Override
	public RangeFilterCategory categorize(InputRow inputRow) {
		String value = inputRow.getValue(column);
		return categorize(value);
	}

	protected RangeFilterCategory categorize(String value) {
		if (value == null) {
			return RangeFilterCategory.LOWER;
		}
		if (value.compareTo(lowestValue) < 0) {
			return RangeFilterCategory.LOWER;
		}
		if (value.compareTo(highestValue) > 0) {
			return RangeFilterCategory.HIGHER;
		}
		return RangeFilterCategory.VALID;
	}
}
