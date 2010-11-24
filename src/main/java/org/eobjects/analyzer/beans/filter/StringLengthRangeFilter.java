package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("String length range")
@Description("Filter rows based on the length of strings.")
public class StringLengthRangeFilter implements Filter<RangeFilterCategory> {

	@Configured
	InputColumn<String> column;

	@Configured
	int minimumLength = 0;

	@Configured
	int maximumLength = 10;

	@Override
	public RangeFilterCategory categorize(InputRow inputRow) {
		String value = inputRow.getValue(column);
		if (value == null) {
			return RangeFilterCategory.LOWER;
		}

		int length = value.length();
		if (length < minimumLength) {
			return RangeFilterCategory.LOWER;
		}

		if (length > maximumLength) {
			return RangeFilterCategory.HIGHER;
		}

		return RangeFilterCategory.VALID;
	}

}
