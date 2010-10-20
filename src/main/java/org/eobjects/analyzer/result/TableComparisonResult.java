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
