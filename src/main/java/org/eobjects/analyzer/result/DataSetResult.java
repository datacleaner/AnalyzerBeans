package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.InMemoryDataSet;
import dk.eobjects.metamodel.data.Row;

public class DataSetResult implements TableModelResult, AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private transient DataSet dataSet;

	// this class uses a list of rows in order to make it serializable (a
	// DataSet is not serializable)
	private List<Row> _rows;

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
