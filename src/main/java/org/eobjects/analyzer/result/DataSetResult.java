package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.Analyzer;

import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.InMemoryDataSet;
import dk.eobjects.metamodel.data.Row;

public class DataSetResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private transient DataSet dataSet;

	// this class uses a list of rows in order to make it serializable (a
	// DataSet is not serializable)
	private List<Row> rows;
	private Class<? extends Analyzer<?>> producerClass;

	public DataSetResult(List<Row> rows, Class<? extends Analyzer<?>> producerClass) {
		this.rows = rows;
		this.producerClass = producerClass;
	}

	public DataSetResult(DataSet ds, Class<? extends Analyzer<?>> producerClass) {
		this.producerClass = producerClass;
		this.rows = new ArrayList<Row>();
		while (ds.next()) {
			rows.add(ds.getRow());
		}
		ds.close();
	}

	public List<Row> getRows() {
		return rows;
	}

	public DataSet getDataSet() {
		if (dataSet == null) {
			dataSet = new InMemoryDataSet(rows);
		}
		return dataSet;
	}

	@Override
	public Class<? extends Analyzer<?>> getProducerClass() {
		return this.producerClass;
	}

}
