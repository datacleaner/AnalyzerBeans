package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.CharIterator;

@FilterBean("Single word")
public class SingleWordFilter implements Filter<ValidationCategory> {

	@Configured
	InputColumn<String> input;

	@Override
	public ValidationCategory categorize(InputRow inputRow) {
		String value = inputRow.getValue(input);
		return filter(value);
	}

	protected ValidationCategory filter(String value) {
		if (value == null || value.length() == 0) {
			return ValidationCategory.INVALID;
		}
		CharIterator it = new CharIterator(value);
		while (it.hasNext()) {
			it.next();
			if (!it.isLetter()) {
				return ValidationCategory.INVALID;
			}
		}
		return ValidationCategory.VALID;
	}

}
