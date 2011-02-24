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
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
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
	public boolean isOptimizable(ValidationCategory category) {
		return true;
	}

	@Override
	public Query optimizeQuery(Query q, ValidationCategory category) {
		if (category == ValidationCategory.VALID) {
			for (InputColumn<?> col : columns) {
				Column column = col.getPhysicalColumn();
				q.where(column, OperatorType.DIFFERENT_FROM, null);
				if (considerEmptyStringAsNull) {
					q.where(column, OperatorType.DIFFERENT_FROM, "");
				}
			}
		} else {
			for (InputColumn<?> col : columns) {
				Column column = col.getPhysicalColumn();

				SelectItem selectItem = new SelectItem(column);
				FilterItem fi1 = new FilterItem(selectItem, OperatorType.EQUALS_TO, null);
				if (considerEmptyStringAsNull) {
					FilterItem fi2 = new FilterItem(selectItem, OperatorType.EQUALS_TO, "");
					q.where(new FilterItem(fi1, fi2));
				} else {
					q.where(fi1);
				}
			}
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
