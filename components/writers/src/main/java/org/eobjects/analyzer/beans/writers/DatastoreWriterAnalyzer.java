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
package org.eobjects.analyzer.beans.writers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.insert.RowInsertionBuilder;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Action;

@AnalyzerBean("Write to database")
public class DatastoreWriterAnalyzer implements Analyzer<WriterResult>,
		Action<Queue<Object[]>> {

	@Inject
	@Configured
	@Description("Values to write to the datastore")
	InputColumn<?>[] values;

	@Inject
	@Configured
	@Description("Datastore to write to")
	Datastore datastore;

	@Inject
	@Configured(required = false)
	@Description("Table to write to")
	String tableName;

	@Inject
	@Configured(required = false)
	@Description("Target schema")
	String targetschema;

	@Inject
	@Configured(required = false)
	@Description("Names of target columns. If not filled, target column order will be assumed.")
	String[] targetColumns;

	private final WriteBuffer writeBuffer;

	public DatastoreWriterAnalyzer() {
		writeBuffer = new WriteBuffer(5000, this);
	}

	@Initialize
	public void init() throws IllegalArgumentException {
		DataContextProvider dcp = datastore.getDataContextProvider();
		try {
			DataContext dc = dcp.getDataContext();
			if (!(dc instanceof UpdateableDataContext)) {
				throw new IllegalArgumentException("Datastore '"
						+ datastore.getName() + "' is not writable");
			}

			final Schema schema = getSchema(dc);

			if (schema == null) {
				throw new IllegalArgumentException(
						"Schema not found. Available schemas names are: "
								+ Arrays.toString(dc.getSchemaNames()));
			}

			final Table table = getTable(schema);

			if (table == null) {
				throw new IllegalArgumentException(
						"Table not found. Available table names are: "
								+ Arrays.toString(schema.getTableNames()));
			}

			if (targetColumns == null) {
				if (table.getColumnCount() != values.length) {
					throw new IllegalArgumentException(
							"Value count and target column count does not match. Table contains "
									+ table.getColumnCount() + " columns but "
									+ values.length + " values provided.");
				}
			} else {
				List<String> columnsNotFound = new ArrayList<String>();
				for (String columnName : targetColumns) {
					if (table.getColumnByName(columnName) == null) {
						columnsNotFound.add(columnName);
					}
				}

				if (!columnsNotFound.isEmpty()) {
					throw new IllegalArgumentException(
							"Could not find columns: " + columnsNotFound);
				}
			}

		} finally {
			dcp.close();
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		Object[] rowData = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			Object value = row.getValue(values[i]);
			rowData[i] = value;
		}
		writeBuffer.addToBuffer(rowData);
	}

	@Override
	public WriterResult getResult() {
		writeBuffer.flushBuffer();

		// TODO Auto-generated method stub
		return new WriterResult();
	}

	@Override
	public void run(final Queue<Object[]> buffer) throws Exception {
		DataContextProvider dcp = datastore.getDataContextProvider();
		try {
			final UpdateableDataContext dc = (UpdateableDataContext) dcp
					.getDataContext();
			dc.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					for (Object[] rowData = buffer.poll(); rowData != null; rowData = buffer
							.poll()) {
						Schema schema = getSchema(dc);
						Table table = getTable(schema);
						RowInsertionBuilder insertBuilder = callback
								.insertInto(table);
						if (targetColumns == null) {
							for (int i = 0; i < rowData.length; i++) {
								insertBuilder = insertBuilder.value(i,
										rowData[i]);
							}
						} else {
							for (int i = 0; i < rowData.length; i++) {
								String columnName = targetColumns[i];
								insertBuilder = insertBuilder.value(columnName,
										rowData[i]);
							}
						}
						insertBuilder.execute();
					}
				}
			});
		} finally {
			dcp.close();
		}
	}

	private Table getTable(final Schema schema) {
		final Table table;
		if (tableName == null) {
			if (schema.getTableCount() == 1) {
				table = schema.getTables()[0];
			} else {
				throw new IllegalArgumentException(
						"No table name specified, and multiple options exist. Available table names are: "
								+ Arrays.toString(schema.getTableNames()));
			}
		} else {
			table = schema.getTableByName(tableName);
		}
		return table;
	}

	private Schema getSchema(DataContext dc) {
		final Schema schema;
		if (targetschema == null) {
			schema = dc.getDefaultSchema();
		} else {
			schema = dc.getSchemaByName(targetschema);
		}
		return schema;
	}
}
