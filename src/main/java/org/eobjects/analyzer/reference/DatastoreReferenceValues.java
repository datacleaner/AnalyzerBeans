package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.connection.DataContextProvider;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;

public class DatastoreReferenceValues implements ReferenceValues<String> {

	private DataContextProvider _dataContextProvider;
	private Column _column;

	public DatastoreReferenceValues(DataContextProvider dataContextProvider, Column column) {
		_dataContextProvider = dataContextProvider;
		_column = column;
	}

	@Override
	public boolean containsValue(String value) {
		DataContext dataContext = _dataContextProvider.getDataContext();

		Query q = dataContext.query().from(_column.getTable()).selectCount().where(_column).equals(value).toQuery();

		DataSet dataSet = dataContext.executeQuery(q);

		assert dataSet.next();

		Number count = (Number) dataSet.getRow().getValue(0);

		assert !dataSet.next();

		if (count == null || count.intValue() == 0) {
			return false;
		}
		return true;
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
