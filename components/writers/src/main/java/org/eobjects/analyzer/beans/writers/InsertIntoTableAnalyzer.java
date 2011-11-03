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
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.UpdateableDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.insert.RowInsertionBuilder;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.util.Action;

@AnalyzerBean("Insert into table")
@Categorized(WriteDataCategory.class)
public class InsertIntoTableAnalyzer implements Analyzer<WriterResult>,
		Action<Queue<Object[]>> {

	@Inject
	@Configured
	@Description("Values to write to the table")
	InputColumn<?>[] values;
	
	@Inject
	@Configured
	@Description("Names of target columns in the table.")
	String[] targetColumns;

	@Inject
	@Configured
	@Description("Datastore to write to")
	UpdateableDatastore datastore;

	@Inject
	@Configured(required = false)
	@Description("Table to write to")
	String tableName;

	@Inject
	@Configured(required = false)
	String targetschema;

	private WriteBuffer _writeBuffer;

	@Initialize
	public void init() throws IllegalArgumentException {
		final int maxObjectsInBuffer = 100000;

		// add one, because there is a small "per record" overhead
		final int objectsPerRow = values.length + 1;

		final int bufferSize = maxObjectsInBuffer / objectsPerRow;

		_writeBuffer = new WriteBuffer(bufferSize, this);

		final UpdateableDatastoreConnection con = datastore.openConnection();
		try {
			final SchemaNavigator schemaNavigator = con.getSchemaNavigator();

			final Column[] columns = schemaNavigator.convertToColumns(
					targetschema, tableName, targetColumns);
			final List<String> columnsNotFound = new ArrayList<String>();
			for (int i = 0; i < columns.length; i++) {
				if (columns[i] == null) {
					columnsNotFound.add(targetColumns[i]);
				}
			}

			if (!columnsNotFound.isEmpty()) {
				throw new IllegalArgumentException("Could not find column(s): "
						+ columnsNotFound);
			}
		} finally {
			con.close();
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		Object[] rowData = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			Object value = row.getValue(values[i]);
			rowData[i] = value;
		}
		_writeBuffer.addToBuffer(rowData);
	}

	@Override
	public WriterResult getResult() {
		_writeBuffer.flushBuffer();

		// TODO Auto-generated method stub
		return new WriterResult();
	}

	@Override
	public void run(final Queue<Object[]> buffer) throws Exception {
		DatastoreConnection con = datastore.openConnection();
		try {
			final Column[] columns = con.getSchemaNavigator().convertToColumns(targetschema, tableName, targetColumns);
			final UpdateableDataContext dc = (UpdateableDataContext) con
					.getDataContext();
			dc.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					for (Object[] rowData = buffer.poll(); rowData != null; rowData = buffer
							.poll()) {
						RowInsertionBuilder insertBuilder = callback
								.insertInto(columns[0].getTable());
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
			con.close();
		}
	}
}
