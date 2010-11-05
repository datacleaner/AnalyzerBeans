package org.eobjects.analyzer.result;

import org.eobjects.analyzer.data.InputColumn;

public class NumberAnalyzerResult extends CrosstabResult {

	private static final long serialVersionUID = 1L;

	private final InputColumn<? extends Number>[] _columns;

	public NumberAnalyzerResult(InputColumn<? extends Number>[] columns, Crosstab<?> crosstab) {
		super(crosstab);
		_columns = columns;
	}

	public InputColumn<? extends Number>[] getColumns() {
		return _columns;
	}
}
