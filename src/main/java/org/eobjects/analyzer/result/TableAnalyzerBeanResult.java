package org.eobjects.analyzer.result;

import dk.eobjects.metamodel.schema.Table;

/**
 * An AnalysisResult for analysises that pertain to a single table.
 */
public interface TableAnalyzerBeanResult extends AnalyzerBeanResult {

	public void setTable(Table table);

	public Table getTable();

}