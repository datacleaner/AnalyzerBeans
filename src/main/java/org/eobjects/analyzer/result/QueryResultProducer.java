package org.eobjects.analyzer.result;

import java.util.LinkedList;
import java.util.List;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.FilteredDataSet;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.query.Query;

public class QueryResultProducer implements ResultProducer {

	private final Query query;
	private final List<SerializableRowFilter> filters;
	private DataContext _dataContext;

	public QueryResultProducer(Query query) {
		if (query == null) {
			throw new IllegalArgumentException("query cannot be null");
		}
		this.query = query;
		this.filters = new LinkedList<SerializableRowFilter>();
	}

	public void addFilter(SerializableRowFilter filter) {
		filters.add(filter);
	}

	@Override
	public AnalyzerResult getResult() {
		DataSet ds = _dataContext.executeQuery(query);
		if (filters != null && !filters.isEmpty()) {
			ds = new FilteredDataSet(ds, filters.toArray(new IRowFilter[filters.size()]));
		}
		return new DataSetResult(ds);
	}

	@Override
	public void setDataContext(DataContext dataContext) {
		_dataContext = dataContext;
	}

}
