package org.eobjects.analyzer.engine;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.TableAnalysisResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class SharedQueryRunnerGroup {

	private Table _table;
	private List<Object[]> _analyzersAndRunDescriptors;
	private Set<Column> _columns = new HashSet<Column>();

	public SharedQueryRunnerGroup(Table table) {
		_table = table;
		_analyzersAndRunDescriptors = new LinkedList<Object[]>();
	}

	public Table getTable() {
		return _table;
	}

	public Set<Column> getColumns() {
		return Collections.unmodifiableSet(_columns);
	}

	public void registerAnalyzer(Object analyzerBean, AnalyzerBeanDescriptor descriptor, Column[] columns) {
		_analyzersAndRunDescriptors.add(new Object[] { analyzerBean, descriptor });
		for (Column column : columns) {
			_columns.add(column);
		}
	}

	public void run(DataContext dataContext) {
		Column[] columns = _columns.toArray(new Column[_columns.size()]);
		Query q = new Query();
		q.select(columns);
		SelectItem countAllItem = SelectItem.getCountAllItem();
		q.select(countAllItem);
		q.from(_table);
		q.groupBy(columns);

		DataSet dataSet = null;
		try {
			dataSet = dataContext.executeQuery(q);
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				Long count;
				Object countValue = row.getValue(countAllItem);
				if (countValue instanceof Long) {
					count = (Long) countValue;
				} else if (countValue instanceof Number) {
					count = ((Number) countValue).longValue();
				} else {
					count = new Long(countValue.toString());
				}

				processRow(row, count);
			}
		} catch (RuntimeException e) {
			if (dataSet != null) {
				dataSet.close();
			}
			throw e;
		}
	}

	private void processRow(Row row, Long count) {
		for (Object[] analyzerAndRunDescriptor : _analyzersAndRunDescriptors) {
			Object analyzerBean = analyzerAndRunDescriptor[0];
			AnalyzerBeanDescriptor analyzerBeanDescriptor = (AnalyzerBeanDescriptor) analyzerAndRunDescriptor[1];
			List<RunDescriptor> runDescriptors = analyzerBeanDescriptor.getRunDescriptors();
			for (RunDescriptor runDescriptor : runDescriptors) {
				runDescriptor.processRow(analyzerBean, row, count);
			}
		}
	}

	public List<TableAnalysisResult> getResults() {
		List<TableAnalysisResult> results = new LinkedList<TableAnalysisResult>();
		for (Object[] analyzerAndRunDescriptor : _analyzersAndRunDescriptors) {
			Object analyzerBean = analyzerAndRunDescriptor[0];
			AnalyzerBeanDescriptor analyzerBeanDescriptor = (AnalyzerBeanDescriptor) analyzerAndRunDescriptor[1];
			List<AnalysisResult> analysisResults = ResultDescriptor.getResults(analyzerBean, analyzerBeanDescriptor);
			for (AnalysisResult analysisResult : analysisResults) {
				results.add((TableAnalysisResult) analysisResult);
			}
		}
		return results;
	}

	@Override
	public String toString() {
		return "SharedQueryRunnerGroup[table=" + _table.getQualifiedLabel() + ",columns=" + _columns.size()
				+ ",analyzerBeans=" + _analyzersAndRunDescriptors.size() + "]";
	}
}
