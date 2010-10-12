package org.eobjects.analyzer.result;

import java.util.List;

import org.eobjects.analyzer.data.InputColumn;

public class ValidationResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private List<Object[]> invalidRows;
	private String[] columnNames;

	public ValidationResult(InputColumn<?>[] inputColumns, List<Object[]> invalidRows) {
		this.invalidRows = invalidRows;
		this.columnNames = new String[inputColumns.length];
		for (int i = 0; i < inputColumns.length; i++) {
			columnNames[i] = inputColumns[i].getName();
		}
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public List<Object[]> getInvalidRows() {
		return invalidRows;
	}
}
