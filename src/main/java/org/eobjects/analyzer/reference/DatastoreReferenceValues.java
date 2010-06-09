package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.connection.DataContextProvider;

import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;

public class DatastoreReferenceValues implements ReferenceValues<String> {

	private DataContextProvider _dataContextProvider;
	private Column _column;

	public DatastoreReferenceValues(DataContextProvider dataContextProvider,
			Column column) {
		_dataContextProvider = dataContextProvider;
		_column = column;
	}

	@Override
	public boolean containsValue(String value) {
		Query q = new Query();
		q.selectCount();
		q.from(_column.getTable());
		q.where(_column, OperatorType.EQUALS_TO, value);

		DataSet dataSet = _dataContextProvider.getDataContext().executeQuery(q);

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
		Query q = new Query();
		q.selectDistinct();
		q.select(_column);
		q.from(_column.getTable());

		DataSet dataSet = _dataContextProvider.getDataContext().executeQuery(q);
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
