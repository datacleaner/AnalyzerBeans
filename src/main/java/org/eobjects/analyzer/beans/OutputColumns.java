package org.eobjects.analyzer.beans;

import java.io.Serializable;

/**
 * Represents the output columns yielded by a Transformer given a certain
 * configuration.
 * 
 * @author Kasper Sørensen
 */
public class OutputColumns implements Serializable {

	private static final long serialVersionUID = 1L;

	private static OutputColumns singleOutputColumn = new OutputColumns(1);

	public static OutputColumns singleOutputColumn() {
		return singleOutputColumn;
	}

	private String[] columnNames;

	public OutputColumns(int columns) {
		if (columns < 1) {
			throw new IllegalArgumentException("columns must be 1 or higher");
		}
		columnNames = new String[columns];
	}

	public OutputColumns(String firstColumnName,
			String... additionalColumnNames) {
		columnNames = new String[additionalColumnNames.length + 1];
		columnNames[0] = firstColumnName;
		for (int i = 0; i < additionalColumnNames.length; i++) {
			columnNames[i + 1] = additionalColumnNames[i];
		}
	}

	public String getColumnName(int index) {
		return columnNames[index];
	}

	public int getColumnCount() {
		return columnNames.length;
	}
}
