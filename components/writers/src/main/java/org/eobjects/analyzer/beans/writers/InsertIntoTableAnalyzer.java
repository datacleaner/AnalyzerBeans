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
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Insert into table")
@Categorized(WriteDataCategory.class)
public class InsertIntoTableAnalyzer implements Analyzer<WriterResult>,
		Action<Queue<Object[]>> {

	private static final Logger logger = LoggerFactory
			.getLogger(InsertIntoTableAnalyzer.class);

	@Inject
	@Configured
	@Description("Values to write to the table")
	InputColumn<?>[] values;

	@Inject
	@Configured
	@Description("Names of columns in the target table.")
	String[] columnNames;

	@Inject
	@Configured
	@Description("Datastore to write to")
	UpdateableDatastore datastore;
	
	@Inject
	@Configured(required = false)
	@Description("Schema name of target table")
	String schemaName;

	@Inject
	@Configured(required = false)
	@Description("Table to target (insert into)")
	String tableName;

	private WriteBuffer _writeBuffer;

	@Initialize
	public void init() throws IllegalArgumentException {
		if (logger.isDebugEnabled()) {
			logger.debug("At init() time, InputColumns are: {}",
					Arrays.toString(values));
		}
		
		final int maxObjectsInBuffer = 100000;

		// add one, because there is a small "per record" overhead
		final int objectsPerRow = values.length + 1;

		final int bufferSize = maxObjectsInBuffer / objectsPerRow;

		logger.info("Row buffer size set to {}", bufferSize);

		_writeBuffer = new WriteBuffer(bufferSize, this);

		final UpdateableDatastoreConnection con = datastore.openConnection();
		try {
			final SchemaNavigator schemaNavigator = con.getSchemaNavigator();

			final Column[] columns = schemaNavigator.convertToColumns(
					schemaName, tableName, columnNames);
			final List<String> columnsNotFound = new ArrayList<String>();
			for (int i = 0; i < columns.length; i++) {
				if (columns[i] == null) {
					columnsNotFound.add(columnNames[i]);
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
		if (logger.isDebugEnabled()) {
			logger.debug("At run() time, InputColumns are: {}",
					Arrays.toString(values));
		}
		
		final Object[] rowData = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			Object value = row.getValue(values[i]);
			rowData[i] = value;

			if (logger.isDebugEnabled()) {
				logger.debug("Value for {} set to: {}", values[i].getName(),
						value);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Adding row data to buffer: {}",
					Arrays.toString(rowData));
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
		UpdateableDatastoreConnection con = datastore.openConnection();
		try {
			final Column[] columns = con.getSchemaNavigator().convertToColumns(
					schemaName, tableName, columnNames);

			if (logger.isDebugEnabled()) {
				logger.debug("Inserting into columns: {}",
						Arrays.toString(columns));
			}

			final UpdateableDataContext dc = con.getUpdateableDataContext();
			dc.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					for (Object[] rowData = buffer.poll(); rowData != null; rowData = buffer
							.poll()) {
						RowInsertionBuilder insertBuilder = callback
								.insertInto(columns[0].getTable());
						for (int i = 0; i < rowData.length; i++) {
							insertBuilder = insertBuilder.value(columns[i],
									rowData[i]);
						}

						if (logger.isDebugEnabled()) {
							logger.debug("Inserting: {}",
									Arrays.toString(rowData));
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
