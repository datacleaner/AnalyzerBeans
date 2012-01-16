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
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.metamodel.query.FilterItem;
import org.eobjects.metamodel.query.OperatorType;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.query.SelectItem;
import org.eobjects.metamodel.schema.Column;

@FilterBean("Equals")
@Description("A filter that excludes values that are not equal (=) to specific set of valid values")
public class EqualsFilter implements QueryOptimizedFilter<ValidationCategory> {

	@Configured
	@Description("Select the column to compare with value(s)")
	InputColumn<?> column;

	@Configured
	@Description("Accepted value(s) for inclusion/validation")
	String[] values;

	private Object[] operands;
	private boolean number = false;

	public EqualsFilter() {
	}

	public EqualsFilter(String[] values, InputColumn<?> column) {
		this();
		this.values = values;
		this.column = column;
		init();
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	@Initialize
	public void init() {
		DataTypeFamily dataTypeFamily = column.getDataTypeFamily();
		operands = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			final String value = values[i];
			final Object operand;
			if (dataTypeFamily == DataTypeFamily.BOOLEAN) {
				operand = ConvertToBooleanTransformer.transformValue(value, ConvertToBooleanTransformer.DEFAULT_TRUE_TOKENS,
						ConvertToBooleanTransformer.DEFAULT_FALSE_TOKENS);
			} else if (dataTypeFamily == DataTypeFamily.DATE) {
				operand = ConvertToDateTransformer.getInternalInstance().transformValue(value);
			} else if (dataTypeFamily == DataTypeFamily.NUMBER) {
				operand = ConvertToNumberTransformer.transformValue(value);
				number = true;
			} else {
				operand = ConvertToStringTransformer.transformValue(value);
			}
			operands[i] = operand;
		}
	}

	@Override
	public ValidationCategory categorize(InputRow inputRow) {
		Object v = inputRow.getValue(column);
		return filter(v);
	}

	public ValidationCategory filter(Object v) {
		if (v == null) {
			for (Object obj : operands) {
				if (obj == null) {
					return ValidationCategory.VALID;
				}
			}
			return ValidationCategory.INVALID;
		}

		for (Object operand : operands) {
			if (number) {
				Number n1 = (Number) operand;
				Number n2 = (Number) v;
				if (equals(n1, n2)) {
					return ValidationCategory.VALID;
				}
			}
			if (operand.equals(v)) {
				return ValidationCategory.VALID;
			}
		}

		return ValidationCategory.INVALID;
	}

	private boolean equals(Number n1, Number n2) {
		if (n1 instanceof Short || n1 instanceof Integer || n1 instanceof Long || n2 instanceof Short
				|| n2 instanceof Integer || n2 instanceof Long) {
			// use long comparision
			return n1.longValue() == n2.longValue();
		}
		return n1.doubleValue() == n2.doubleValue();
	}

	@Override
	public boolean isOptimizable(ValidationCategory category) {
		return true;
	}

	@Override
	public Query optimizeQuery(Query q, ValidationCategory category) {
		Column physicalColumn = column.getPhysicalColumn();
		if (category == ValidationCategory.VALID) {
			List<FilterItem> filterItems = new ArrayList<FilterItem>();
			SelectItem selectItem = new SelectItem(physicalColumn);
			for (Object operand : operands) {
				filterItems.add(new FilterItem(selectItem, OperatorType.EQUALS_TO, operand));
			}
			q.where(new FilterItem(filterItems.toArray(new FilterItem[filterItems.size()])));
		} else {
			for (Object operand : operands) {
				q.where(column.getPhysicalColumn(), OperatorType.DIFFERENT_FROM, operand);
			}
		}
		return q;
	}
}
