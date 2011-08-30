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

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.metamodel.query.Query;

@FilterBean("Max rows")
@Description("Sets a maximum of rows to process.")
public class MaxRowsFilter implements QueryOptimizedFilter<ValidationCategory> {

	@Configured
	int maxRows = 1000;

	private final AtomicInteger counter = new AtomicInteger();

	public MaxRowsFilter() {
	}

	public MaxRowsFilter(int maxRows) {
		this();
		this.maxRows = maxRows;
	}
	
	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}
	
	public int getMaxRows() {
		return maxRows;
	}

	@Override
	public ValidationCategory categorize(InputRow inputRow) {
		int count = counter.incrementAndGet();
		if (count > maxRows) {
			return ValidationCategory.INVALID;
		}
		return ValidationCategory.VALID;
	}

	@Override
	public boolean isOptimizable(ValidationCategory category) {
		// can only optimize the valid records
		return category == ValidationCategory.VALID;
	}

	@Override
	public Query optimizeQuery(Query q, ValidationCategory category) {
		if (category == ValidationCategory.VALID) {
			q.setMaxRows(maxRows);
		} else {
			throw new IllegalStateException("Can only optimize the VALID max rows category");
		}
		return q;
	}

}
