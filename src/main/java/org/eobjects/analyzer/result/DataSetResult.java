package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.List;

import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;

public class DataSetResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private transient DataSet dataSet;

	// this class uses a list of rows in order to make it serializable (a
	// DataSet is not serializable)
	private List<Row> rows;
	private Class<?> producerClass;

	public DataSetResult(List<Row> rows, Class<?> producerClass) {
		this.rows = rows;
		this.producerClass = producerClass;
	}

	public DataSetResult(DataSet ds,
			Class<? extends QueryResultProducer> producerClass) {
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
			dataSet = new DataSet(rows);
		}
		return dataSet;
	}

	@Override
	public Class<?> getProducerClass() {
		return this.producerClass;
	}

}
