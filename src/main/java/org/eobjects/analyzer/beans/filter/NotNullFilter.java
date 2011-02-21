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
import org.eobjects.analyzer.util.StringUtils;

@FilterBean("Not null")
@Description("Filter rows that contain null values.")
public class NotNullFilter implements Filter<ValidationCategory> {

	@Configured
	InputColumn<?>[] columns;

	@Configured
	@Description("Consider empty strings (\"\") as null also?")
	boolean considerEmptyStringAsNull = false;

	public NotNullFilter() {
	}

	public NotNullFilter(InputColumn<?>[] columns, boolean considerEmptyStringAsNull) {
		this();
		this.columns = columns;
		this.considerEmptyStringAsNull = considerEmptyStringAsNull;
	}

	@Override
	public ValidationCategory categorize(InputRow inputRow) {
		for (InputColumn<?> col : columns) {
			Object value = inputRow.getValue(col);
			if (value == null) {
				return ValidationCategory.INVALID;
			}

			if (considerEmptyStringAsNull && StringUtils.isNullOrEmpty(value.toString())) {
				return ValidationCategory.INVALID;
			}
		}
		return ValidationCategory.VALID;
	}
}
