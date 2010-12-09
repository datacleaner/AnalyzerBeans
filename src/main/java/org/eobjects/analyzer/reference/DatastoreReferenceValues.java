/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;

public final class DatastoreReferenceValues implements ReferenceValues<String> {

	private final Datastore _datastore;
	private final Column _column;
	private final WeakHashMap<String, Boolean> _containsValueCache = new WeakHashMap<String, Boolean>();

	public DatastoreReferenceValues(Datastore datastore, Column column) {
		_datastore = datastore;
		_column = column;
	}

	public void clearCache() {
		_containsValueCache.clear();
	}

	@Override
	public boolean containsValue(String value) {
		Boolean result = _containsValueCache.get(value);
		if (result == null) {
			synchronized (_containsValueCache) {
				result = _containsValueCache.get(value);
				if (result == null) {
					result = false;
					DataContextProvider dcp = _datastore.getDataContextProvider();
					DataContext dataContext = dcp.getDataContext();
					Query q = dataContext.query().from(_column.getTable()).selectCount().where(_column).equals(value)
							.toQuery();
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
					dataSet.close();
					dcp.close();
					_containsValueCache.put(value, result);
				}
			}
		}
		return result;

	}

	@Override
	public Collection<String> getValues() {
		DataContextProvider dcp = _datastore.getDataContextProvider();
		DataContext dataContext = dcp.getDataContext();

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
		dataSet.close();
		dcp.close();
		return values;
	}
}
