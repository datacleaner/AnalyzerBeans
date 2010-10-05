package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.CompareColumnsAnalyzer;
import org.eobjects.analyzer.beans.api.Analyzer;

public final class ColumnComparisonResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	
	private List<ColumnDifference<?>> columnDifferences;

	public ColumnComparisonResult(Collection<ColumnDifference<?>> columnDifferences) {
		if (columnDifferences == null) {
			throw new IllegalArgumentException("columnDifferences cannot be null");
		}
		this.columnDifferences = Collections
				.unmodifiableList(new ArrayList<ColumnDifference<?>>(
						columnDifferences));
	}

	@Override
	public Class<? extends Analyzer<?>> getProducerClass() {
		return CompareColumnsAnalyzer.class;
	}
	
	public List<ColumnDifference<?>> getColumnDifferences() {
		return columnDifferences;
	}
	
	public boolean isColumnsEqual() {
		return columnDifferences.isEmpty();
	}
}
