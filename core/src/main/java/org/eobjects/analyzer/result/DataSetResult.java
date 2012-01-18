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
package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.InMemoryDataSet;
import org.eobjects.metamodel.data.Row;

public class DataSetResult implements TableModelResult, AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private transient DataSet dataSet;

	// this class uses a list of rows in order to make it serializable (a
	// DataSet is not serializable)
	private final List<Row> _rows;

	public DataSetResult(List<Row> rows) {
		_rows = rows;
	}

	public DataSetResult(DataSet ds) {
		_rows = new ArrayList<Row>();
		while (ds.next()) {
			_rows.add(ds.getRow());
		}
		ds.close();
	}

	public List<Row> getRows() {
		return _rows;
	}

	public DataSet getDataSet() {
		if (dataSet == null) {
			dataSet = new InMemoryDataSet(_rows);
		}
		return dataSet;
	}

	@Override
	public TableModel toTableModel() {
		return getDataSet().toTableModel();
	}
}
