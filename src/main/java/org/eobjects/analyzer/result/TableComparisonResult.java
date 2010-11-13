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
package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class TableComparisonResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private List<TableDifference<?>> tableDifferences;
	private List<ColumnComparisonResult> columnComparisonResults;

	public TableComparisonResult(Collection<TableDifference<?>> tableDifferences,
			Collection<ColumnComparisonResult> columnComparisonResults) {
		if (tableDifferences == null) {
			throw new IllegalArgumentException("tableDifferences cannot be null");
		}
		if (columnComparisonResults == null) {
			throw new IllegalArgumentException("columnComparisonResults cannot be null");
		}
		this.tableDifferences = Collections.unmodifiableList(new ArrayList<TableDifference<?>>(tableDifferences));
		this.columnComparisonResults = Collections.unmodifiableList(new ArrayList<ColumnComparisonResult>(
				columnComparisonResults));
	}

	public List<TableDifference<?>> getTableDifferences() {
		return tableDifferences;
	}

	public List<ColumnComparisonResult> getColumnComparisonResults() {
		return columnComparisonResults;
	}

	public boolean isTablesEqual() {
		return tableDifferences.isEmpty() && columnComparisonResults.isEmpty();
	}
}
