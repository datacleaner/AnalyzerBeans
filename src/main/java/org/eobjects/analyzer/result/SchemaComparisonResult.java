package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.CompareSchemasAnalyzer;
import org.eobjects.analyzer.beans.api.Analyzer;

public class SchemaComparisonResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private List<SchemaDifference<?>> schemaDifferences;
	private List<TableComparisonResult> tableComparisonResults;

	public SchemaComparisonResult(
			Collection<SchemaDifference<?>> schemaDifferences,
			Collection<TableComparisonResult> tableComparisonResults) {
		if (schemaDifferences == null) {
			throw new IllegalArgumentException(
					"schemaDifferences cannot be null");
		}
		if (tableComparisonResults == null) {
			throw new IllegalArgumentException(
					"tableComparisonResults cannot be null");
		}
		this.schemaDifferences = Collections
				.unmodifiableList(new ArrayList<SchemaDifference<?>>(
						schemaDifferences));
		this.tableComparisonResults = Collections
				.unmodifiableList(new ArrayList<TableComparisonResult>(
						tableComparisonResults));
	}

	@Override
	public Class<? extends Analyzer<?>> getProducerClass() {
		return CompareSchemasAnalyzer.class;
	}

	public List<SchemaDifference<?>> getSchemaDifferences() {
		return schemaDifferences;
	}

	public List<TableComparisonResult> getTableComparisonResults() {
		return tableComparisonResults;
	}

	public boolean isSchemasEqual() {
		return schemaDifferences.isEmpty() && tableComparisonResults.isEmpty();
	}
}
