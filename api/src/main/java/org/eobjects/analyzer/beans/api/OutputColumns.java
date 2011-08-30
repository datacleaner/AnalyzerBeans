/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans.api;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents the output columns yielded by a Transformer given a certain
 * configuration.
 * 
 * @author Kasper Sørensen
 */
public final class OutputColumns implements Serializable {

	private static final long serialVersionUID = 1L;

	private static OutputColumns singleOutputColumn = new OutputColumns(1);

	/**
	 * Factory/convenience method for retrieving a single output column
	 * instance.
	 * 
	 * @return a single output column instance.
	 */
	public static OutputColumns singleOutputColumn() {
		return singleOutputColumn;
	}

	private final String[] columnNames;

	/**
	 * Constructs an OutputColumns object with a variable amount of anonymous
	 * columns.
	 * 
	 * @param columns
	 *            the amount of columns.
	 */
	public OutputColumns(int columns) {
		if (columns < 1) {
			throw new IllegalArgumentException("columns must be 1 or higher");
		}
		columnNames = new String[columns];
	}

	/**
	 * Constructs an OutputColumns object with named columns.
	 * 
	 * @param columnNames
	 *            the names of the output columns.
	 */
	public OutputColumns(String[] columnNames) {
		if (columnNames == null) {
			throw new IllegalArgumentException("column cannot be null");
		}
		if (columnNames.length < 1) {
			throw new IllegalArgumentException("columns must be 1 or higher");
		}
		this.columnNames = columnNames.clone();
	}

	/**
	 * Constructs an OutputColumns object with named columns.
	 * 
	 * @param firstColumnName
	 *            the first column name
	 * @param additionalColumnNames
	 *            the additional column names (varargs)
	 */
	public OutputColumns(String firstColumnName, String... additionalColumnNames) {
		columnNames = new String[additionalColumnNames.length + 1];
		columnNames[0] = firstColumnName;
		for (int i = 0; i < additionalColumnNames.length; i++) {
			columnNames[i + 1] = additionalColumnNames[i];
		}
	}

	/**
	 * Gets the column name of a column by index.
	 * 
	 * @param index
	 *            the index of the column.
	 * @return the name of the column.
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}

	/**
	 * Gets the amount of columns in this OutputColumns object.
	 * 
	 * @return an integer representing the amount of columns available.
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String toString() {
		return "OutputColumns" + Arrays.toString(columnNames);
	}
}
