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

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.beans.api.Validate;
import org.eobjects.analyzer.beans.categories.FilterCategory;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.metamodel.query.Query;

@FilterBean("Max rows")
@Description("Sets a maximum number of rows to process.")
@Categorized(FilterCategory.class)
public class MaxRowsFilter implements QueryOptimizedFilter<MaxRowsFilter.Category> {

    public static enum Category {
        VALID, INVALID
    }

    @Configured
    @NumberProperty(negative = false, zero = false)
    @Description("The maximum number of rows to process.")
    int maxRows = 1000;

    @Configured
    @NumberProperty(negative = false, zero = false)
    @Description("The first row (aka 'offset') to process.")
    int firstRow = 1;

    private final AtomicInteger counter = new AtomicInteger();

    public MaxRowsFilter() {
    }

    public MaxRowsFilter(int firstRow, int maxRows) {
        this();
        this.firstRow = firstRow;
        this.maxRows = maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getMaxRows() {
        return maxRows;
    }
    
    public int getFirstRow() {
        return firstRow;
    }
    
    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    @Validate
    public void validate() {
        if (maxRows <= 0) {
            throw new IllegalStateException("Max rows value must be a positive integer");
        }
        if (firstRow <= 0) {
            throw new IllegalStateException("First row value must be a positive integer");
        }
    }

    @Override
    public Category categorize(InputRow inputRow) {
        int count = counter.incrementAndGet();
        if (count < firstRow || count >= maxRows + firstRow) {
            return Category.INVALID;
        }
        return Category.VALID;
    }

    @Override
    public boolean isOptimizable(Category category) {
        // can only optimize the valid records
        return category == Category.VALID;
    }

    @Override
    public Query optimizeQuery(Query q, Category category) {
        if (category == Category.VALID) {
            q.setMaxRows(maxRows);
            
            if (firstRow > 1) {
                q.setFirstRow(firstRow);
            }
        } else {
            throw new IllegalStateException("Can only optimize the VALID max rows category");
        }
        return q;
    }

}
