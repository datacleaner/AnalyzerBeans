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
package org.eobjects.analyzer.beans.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.query.OperatorType;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;

/**
 * A transformer that can do a lookup (like a left join) based on a set of
 * columns in any datastore.
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Datastore lookup")
@Description("Perform a lookup based on a set of columns in any datastore (like a LEFT join).")
@Concurrent(true)
public class DatastoreLookupTransformer implements Transformer<Object> {

	@Configured
	Datastore datastore;

	@Configured
	InputColumn<?>[] conditionValues;

	@Configured
	String[] conditionColumns;

	@Configured
	String[] outputColumns;

	@Configured(required = false)
	@Alias("Schema")
	String schemaName;

	@Configured(required = false)
	@Alias("Table")
	String tableName;

	private final Map<List<Object>, Object[]> cache = CollectionUtils2.createCacheMap();
	private Column[] queryOutputColumns;
	private Column[] queryConditionColumns;

	private Column[] getQueryConditionColumns() {
		if (queryConditionColumns == null) {
			final DatastoreConnection con = datastore.openConnection();
			try {
				queryConditionColumns = con.getSchemaNavigator().convertToColumns(schemaName, tableName, conditionColumns);
			} finally {
				con.close();
			}
		}
		return queryConditionColumns;
	}

	private Column[] getQueryOutputColumns() {
		if (queryOutputColumns == null) {
			final DatastoreConnection con = datastore.openConnection();
			try {
				queryOutputColumns = con.getSchemaNavigator().convertToColumns(schemaName, tableName, outputColumns);
			} finally {
				con.close();
			}
		}
		return queryOutputColumns;
	}

	@Initialize
	public void init() {
		Column[] queryConditionColumns = getQueryConditionColumns();
		final List<String> columnsNotFound = new ArrayList<String>();
		for (int i = 0; i < queryConditionColumns.length; i++) {
			if (queryConditionColumns[i] == null) {
				columnsNotFound.add(conditionColumns[i]);
			}
		}

		if (!columnsNotFound.isEmpty()) {
			throw new IllegalArgumentException("Could not find column(s): " + columnsNotFound);
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		Column[] queryOutputColumns = getQueryOutputColumns();
		String[] names = new String[queryOutputColumns.length];
		Class<?>[] types = new Class[queryOutputColumns.length];
		for (int i = 0; i < queryOutputColumns.length; i++) {
			Column column = queryOutputColumns[i];
			if (column == null) {
				throw new IllegalArgumentException("Could not find column: " + outputColumns[i]);
			}
			names[i] = column.getName() + " (lookup)";
			types[i] = column.getType().getJavaEquivalentClass();
		}
		return new OutputColumns(names, types);
	}

	@Override
	public Object[] transform(InputRow inputRow) {
		List<Object> queryInput = new ArrayList<Object>(conditionValues.length);
		for (InputColumn<?> inputColumn : conditionValues) {
			Object value = inputRow.getValue(inputColumn);
			queryInput.add(value);
		}

		Object[] result;
		synchronized (cache) {
			result = cache.get(queryInput);
			if (result == null) {
				result = performQuery(queryInput);
				cache.put(queryInput, result);
			}
		}

		return result;
	}

	private Object[] performQuery(List<Object> queryInput) {
		final Object[] result;

		final DatastoreConnection con = datastore.openConnection();
		try {
			Column[] queryOutputColumns = getQueryOutputColumns();
			Column[] queryConditionColumns = getQueryConditionColumns();

			Query query = new Query().from(queryOutputColumns[0].getTable()).select(queryOutputColumns);
			for (int i = 0; i < queryConditionColumns.length; i++) {
				query = query.where(queryConditionColumns[i], OperatorType.EQUALS_TO, queryInput.get(i));
			}
			query = query.setMaxRows(1);
			final DataSet dataSet = con.getDataContext().executeQuery(query);
			if (dataSet.next()) {
				result = dataSet.getRow().getValues();
			} else {
				result = new Object[outputColumns.length];
			}
			dataSet.close();
			return result;
		} finally {
			con.close();
		}
	}

	@Close
	public void close() {
		cache.clear();
		queryOutputColumns = null;
		queryConditionColumns = null;
	}
}
