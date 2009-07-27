package org.eobjects.analyzer.result;

import java.util.List;

import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;


public class DataSetResult implements AnalyzerBeanResult {

	private static final long serialVersionUID = 1L;
	private transient DataSet dataSet;
	private List<Row> rows;
	private Class<?> analyzerClass;
	
	public DataSetResult(List<Row> rows, Class<?> analyzerClass) {
		this.rows = rows;
		this.analyzerClass = analyzerClass;
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
	public Class<?> getAnalyzerClass() {
		return this.analyzerClass;
	}

}
