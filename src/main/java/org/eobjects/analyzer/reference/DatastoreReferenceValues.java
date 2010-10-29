package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import org.eobjects.analyzer.connection.DataContextProvider;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;

public final class DatastoreReferenceValues implements ReferenceValues<String> {

	private final DataContextProvider _dataContextProvider;
	private final Column _column;
	private final WeakHashMap<String, Boolean> _containsValueCache = new WeakHashMap<String, Boolean>();

	public DatastoreReferenceValues(DataContextProvider dataContextProvider, Column column) {
		_dataContextProvider = dataContextProvider;
		_column = column;
	}

	public void clearCache() {
		_containsValueCache.clear();
	}

	@Override
	public boolean containsValue(String value) {
		Boolean result = _containsValueCache.get(value);
		if (result == null) {
			result = false;
			DataContext dataContext = _dataContextProvider.getDataContext();
			Query q = dataContext.query().from(_column.getTable()).selectCount().where(_column).equals(value).toQuery();
			DataSet dataSet = dataContext.executeQuery(q);
			if (dataSet.next()) {
				Row row = dataSet.getRow();
				if (row != null) {
					Number count = (Number) row.getValue(0);
					if (count != null && count.intValue() > 0) {
						result = true;
					}
					assert !dataSet.next();
				}
			}
			_containsValueCache.put(value, result);
		}
		return result;

	}

	@Override
	public Collection<String> getValues() {
		DataContext dataContext = _dataContextProvider.getDataContext();

		Query q = dataContext.query().from(_column.getTable()).select(_column).toQuery();
		q.selectDistinct();

		DataSet dataSet = dataContext.executeQuery(q);
		List<String> values = new ArrayList<String>();
		while (dataSet.next()) {
			Row row = dataSet.getRow();
			Object value = row.getValue(0);
			if (value != null) {
				value = value.toString();
			}
			values.add((String) value);
		}
		return values;
	}
}
