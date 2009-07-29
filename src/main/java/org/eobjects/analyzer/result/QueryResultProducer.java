package org.eobjects.analyzer.result;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.annotations.Provided;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.RowFilterDataSetStrategyWrapper;
import dk.eobjects.metamodel.query.Query;

public class QueryResultProducer implements ResultProducer {

	@Provided
	DataContext dataContext;

	private Query query;
	private List<SerializableRowFilter> filters;

	public QueryResultProducer(Query query) {
		if (query == null) {
			throw new NullPointerException("query");
		}
		this.query = query;
	}

	public void addFilter(SerializableRowFilter filter) {
		if (filters == null) {
			filters = new LinkedList<SerializableRowFilter>();
		}
		filters.add(filter);
	}

	public void setFilters(List<SerializableRowFilter> filters) {
		this.filters = filters;
	}

	@Override
	public AnalyzerResult getResult() {
		DataSet ds = dataContext.executeQuery(query);
		if (filters != null && !filters.isEmpty()) {
			ds = new DataSet(new RowFilterDataSetStrategyWrapper(ds, filters
					.toArray(new IRowFilter[filters.size()])));
		}
		return new DataSetResult(ds, getClass());
	}

}
