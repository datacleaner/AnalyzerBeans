package org.eobjects.analyzer.beans;

import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnalyzerResult;

public class ValidationResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	
	private Analyzer analyzer;
	private List<Object[]> invalidRows;
	private String[] columnNames;
	
	public ValidationResult(Analyzer analyzer, InputColumn<?>[] inputColumns, List<Object[]> invalidRows) {
		this.analyzer = analyzer;
		this.invalidRows = invalidRows;
		this.columnNames = new String[inputColumns.length];
		for (int i = 0; i < inputColumns.length; i++) {
			columnNames[i] = inputColumns[i].getName();
		}
	}

	@Override
	public Class<? extends Analyzer> getProducerClass() {
		return analyzer.getClass();
	}

	public String[] getColumnNames() {
		return columnNames;
	}
	
	public List<Object[]> getInvalidRows() {
		return invalidRows;
	}
}
