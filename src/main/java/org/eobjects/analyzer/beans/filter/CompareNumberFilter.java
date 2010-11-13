/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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
