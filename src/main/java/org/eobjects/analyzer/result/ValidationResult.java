package org.eobjects.analyzer.result;

import java.util.List;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.data.InputColumn;

public class ValidationResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private List<Object[]> invalidRows;
	private String[] columnNames;
	private Class<? extends Analyzer<?>> analyzerClass;

	public ValidationResult(Class<? extends Analyzer<?>> analyzerClass,
			InputColumn<?>[] inputColumns, List<Object[]> invalidRows) {
		this.analyzerClass = analyzerClass;
		this.invalidRows = invalidRows;
		this.columnNames = new String[inputColumns.length];
		for (int i = 0; i < inputColumns.length; i++) {
			columnNames[i] = inputColumns[i].getName();
		}
	}

	@Override
	public Class<? extends Analyzer<?>> getProducerClass() {
		return analyzerClass;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public List<Object[]> getInvalidRows() {
		return invalidRows;
	}
}
