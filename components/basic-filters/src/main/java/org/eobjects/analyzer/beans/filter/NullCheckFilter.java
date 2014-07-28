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

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.beans.categories.FilterCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;

@FilterBean("Null check")
@Alias("Not null")
@Description("Filter rows that contain null values.")
@Categorized(FilterCategory.class)
@Distributed(true)
public class NullCheckFilter implements QueryOptimizedFilter<NullCheckFilter.NullCheckCategory> {

    public static enum NullCheckCategory {
        @Alias("INVALID")
        NULL,

        @Alias("VALID")
        NOT_NULL;
    }

    @Configured
    @Description("Select columns that should NOT have null values")
    InputColumn<?>[] columns;

    @Configured
    @Description("Consider empty strings (\"\") as null also?")
    boolean considerEmptyStringAsNull = false;

    public NullCheckFilter() {
    }

    public NullCheckFilter(InputColumn<?>[] columns, boolean considerEmptyStringAsNull) {
        this();
        this.columns = columns;
        this.considerEmptyStringAsNull = considerEmptyStringAsNull;
    }

    public void setConsiderEmptyStringAsNull(boolean considerEmptyStringAsNull) {
        this.considerEmptyStringAsNull = considerEmptyStringAsNull;
    }

    @Override
    public boolean isOptimizable(NullCheckCategory category) {
        return true;
    }

    @Override
    public Query optimizeQuery(Query q, NullCheckCategory category) {
        if (category == NullCheckCategory.NOT_NULL) {
            for (InputColumn<?> col : columns) {
                Column column = col.getPhysicalColumn();
                if (column == null) {
                    throw new IllegalStateException("Cannot optimize on non-physical column: " + col);
                }
                q.where(column, OperatorType.DIFFERENT_FROM, null);
                if (considerEmptyStringAsNull && col.getDataType() == String.class) {
                    q.where(column, OperatorType.DIFFERENT_FROM, "");
                }
            }
        } else {
            // if NULL all filter items will be OR'ed.
            List<FilterItem> filterItems = new ArrayList<FilterItem>();
            for (InputColumn<?> col : columns) {
                Column column = col.getPhysicalColumn();
                if (column == null) {
                    throw new IllegalStateException("Cannot optimize on non-physical column: " + col);
                }

                SelectItem selectItem = new SelectItem(column);
                FilterItem fi1 = new FilterItem(selectItem, OperatorType.EQUALS_TO, null);
                filterItems.add(fi1);
                if (considerEmptyStringAsNull && col.getDataType() == String.class) {
                    FilterItem fi2 = new FilterItem(selectItem, OperatorType.EQUALS_TO, "");
                    filterItems.add(fi2);
                }
            }
            q.where(new FilterItem(filterItems.toArray(new FilterItem[filterItems.size()])));
        }
        return q;
    }

    @Override
    public NullCheckCategory categorize(InputRow inputRow) {
        for (InputColumn<?> col : columns) {
            Object value = inputRow.getValue(col);
            if (value == null) {
                return NullCheckCategory.NULL;
            }

            if (considerEmptyStringAsNull && "".equals(value)) {
                return NullCheckCategory.NULL;
            }
        }
        return NullCheckCategory.NOT_NULL;
    }
}
