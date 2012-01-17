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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.metamodel.query.FilterItem;
import org.eobjects.metamodel.query.OperatorType;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.query.SelectItem;
import org.eobjects.metamodel.schema.Column;

@FilterBean("Not null")
@Description("Filter rows that contain null values.")
public class NotNullFilter implements QueryOptimizedFilter<ValidationCategory> {

	@Configured
	@Description("Select columns that should NOT have null values")
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
	
	public void setConsiderEmptyStringAsNull(boolean considerEmptyStringAsNull) {
		this.considerEmptyStringAsNull = considerEmptyStringAsNull;
	}
	
	@Override
	public boolean isOptimizable(ValidationCategory category) {
		return true;
	}

	@Override
	public Query optimizeQuery(Query q, ValidationCategory category) {
		if (category == ValidationCategory.VALID) {
			for (InputColumn<?> col : columns) {
				Column column = col.getPhysicalColumn();
				q.where(column, OperatorType.DIFFERENT_FROM, null);
				if (considerEmptyStringAsNull && col.getDataTypeFamily() == DataTypeFamily.STRING) {
					q.where(column, OperatorType.DIFFERENT_FROM, "");
				}
			}
		} else {
			// if INVALID all filter items will be OR'ed.
			List<FilterItem> filterItems = new ArrayList<FilterItem>();
			for (InputColumn<?> col : columns) {
				Column column = col.getPhysicalColumn();

				SelectItem selectItem = new SelectItem(column);
				FilterItem fi1 = new FilterItem(selectItem, OperatorType.EQUALS_TO, null);
				filterItems.add(fi1);
				if (considerEmptyStringAsNull && col.getDataTypeFamily() == DataTypeFamily.STRING) {
					FilterItem fi2 = new FilterItem(selectItem, OperatorType.EQUALS_TO, "");
					filterItems.add(fi2);
				}
			}
			q.where(new FilterItem(filterItems.toArray(new FilterItem[filterItems.size()])));
		}
		return q;
	}

	@Override
	public ValidationCategory categorize(InputRow inputRow) {
		for (InputColumn<?> col : columns) {
			Object value = inputRow.getValue(col);
			if (value == null) {
				return ValidationCategory.INVALID;
			}

			if (considerEmptyStringAsNull && "".equals(value)) {
				return ValidationCategory.INVALID;
			}
		}
		return ValidationCategory.VALID;
	}
}
