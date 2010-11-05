package org.eobjects.analyzer.result;

import org.eobjects.analyzer.data.InputColumn;

/**
 * Result type of the StringAnalyzer
 * 
 * @author Kasper Sørensen
 */
public class StringAnalyzerResult extends CrosstabResult {

	private static final long serialVersionUID = 1L;

	private final InputColumn<String>[] _columns;

	public StringAnalyzerResult(InputColumn<String>[] columns, Crosstab<?> crosstab) {
		super(crosstab);
		_columns = columns;
	}

	public InputColumn<String>[] getColumns() {
		return _columns;
	}
}
