package org.eobjects.analyzer.result;

import org.eobjects.analyzer.data.InputColumn;

public class PatternFinderResult extends CrosstabResult {

	private static final long serialVersionUID = 1L;
	private final InputColumn<String> _column;

	public PatternFinderResult(InputColumn<String> column, Crosstab<?> crosstab) {
		super(crosstab);
		_column = column;
	}

	public InputColumn<String> getColumn() {
		return _column;
	}
}
