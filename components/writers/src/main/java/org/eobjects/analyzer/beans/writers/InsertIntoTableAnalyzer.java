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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.create.TableCreationBuilder;
import org.eobjects.metamodel.csv.CsvDataContext;
import org.eobjects.metamodel.insert.RowInsertionBuilder;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Insert into table")
@Description("Insert records into a table in a registered datastore. This component allows you to map the values available in the flow with the columns of the target table.")
@Categorized(WriteDataCategory.class)
public class InsertIntoTableAnalyzer implements Analyzer<WriteDataResult>,
		Action<Queue<Object[]>> {

	private static final File TEMP_DIR = FileHelper.getTempDir();

	private static final String ERROR_MESSAGE_COLUMN_NAME = "insert_into_table_error_message";

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

	@Inject
	@Configured(value = "How to handle insertion errors?")
	ErrorHandlingOption errorHandlingOption = ErrorHandlingOption.STOP_JOB;

	@Inject
	@Configured(value = "Error log file location")
	@Description("Directory or file path for saving errornuos records")
	@FileProperty(accessMode = FileAccessMode.SAVE, extension = ".csv")
	File errorLogFile = TEMP_DIR;

	private WriteBuffer _writeBuffer;
	private AtomicInteger _writtenRowCount;
	private AtomicInteger _errorRowCount;
	private CsvDataContext _errorDataContext;

	@Initialize
	public void init() throws IllegalArgumentException {
		if (logger.isDebugEnabled()) {
			logger.debug("At init() time, InputColumns are: {}",
					Arrays.toString(values));
		}

		_errorRowCount = new AtomicInteger();
		_writtenRowCount = new AtomicInteger();
		if (errorHandlingOption == ErrorHandlingOption.SAVE_TO_FILE) {
			_errorDataContext = createErrorDataContext();
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

	private void validateCsvHeaders(CsvDataContext dc) {
		Schema schema = dc.getDefaultSchema();
		if (schema.getTableCount() == 0) {
			// nothing to worry about, we will create the table ourselves
			return;
		}
		Table table = schema.getTables()[0];

		// verify that table names correspond to what we need!

		for (String columnName : columnNames) {
			Column column = table.getColumnByName(columnName);
			if (column == null) {
				throw new IllegalStateException(
						"Error log file does not have required column header: "
								+ columnName);
			}
		}

		Column column = table.getColumnByName(ERROR_MESSAGE_COLUMN_NAME);
		if (column == null) {
			throw new IllegalStateException(
					"Error log file does not have required column: "
							+ ERROR_MESSAGE_COLUMN_NAME);
		}
	}

	private CsvDataContext createErrorDataContext() {
		final File file;

		if (TEMP_DIR.equals(errorLogFile)) {
			try {
				file = File.createTempFile("insertion_error", ".csv");
			} catch (IOException e) {
				throw new IllegalStateException(
						"Could not create new temp file", e);
			}
		} else if (errorLogFile.isDirectory()) {
			file = new File(errorLogFile, "insertion_error_log.csv");
		} else {
			file = errorLogFile;
		}

		final CsvDataContext dc = new CsvDataContext(file);

		final Schema schema = dc.getDefaultSchema();

		if (file.exists() && file.length() > 0) {
			validateCsvHeaders(dc);
		} else {
			// create table if no table exists.
			dc.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback cb) {
					TableCreationBuilder tableBuilder = cb.createTable(schema,
							"error_table");
					for (String columnName : columnNames) {
						tableBuilder = tableBuilder.withColumn(columnName);
					}

					tableBuilder = tableBuilder
							.withColumn(ERROR_MESSAGE_COLUMN_NAME);

					tableBuilder.execute();
				}
			});
		}

		return dc;
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
				logger.debug("Value for {} set to: {}", columnNames[i], value);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Adding row data to buffer: {}",
					Arrays.toString(rowData));
		}

		for (int i = 0; i < distinctCount; i++) {
			_writeBuffer.addToBuffer(rowData);
		}
	}

	@Override
	public WriteDataResult getResult() {
		_writeBuffer.flushBuffer();

		final int writtenRowCount = _writtenRowCount.get();

		final FileDatastore errorDatastore;
		if (_errorDataContext != null) {
			File file = _errorDataContext.getFile();
			errorDatastore = new CsvDatastore(file.getName(),
					file.getAbsolutePath());
		} else {
			errorDatastore = null;
		}

		return new WriteDataResultImpl(writtenRowCount, datastore, schemaName,
				tableName, _errorRowCount.get(), errorDatastore);
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

						try {
							insertBuilder.execute();
							_writtenRowCount.incrementAndGet();
						} catch (final RuntimeException e) {
							_errorRowCount.incrementAndGet();
							if (errorHandlingOption == ErrorHandlingOption.STOP_JOB) {
								throw e;
							} else {
								logger.warn(
										"Error occurred while inserting record. Writing to error stream",
										e);
								final Object[] rowValues = rowData;
								_errorDataContext
										.executeUpdate(new UpdateScript() {
											@Override
											public void run(UpdateCallback cb) {
												RowInsertionBuilder insertBuilder = cb
														.insertInto(_errorDataContext
																.getDefaultSchema()
																.getTables()[0]);
												for (int i = 0; i < rowValues.length; i++) {
													insertBuilder = insertBuilder
															.value(columnNames[i],
																	rowValues[i]);
												}

												insertBuilder = insertBuilder
														.value(ERROR_MESSAGE_COLUMN_NAME,
																e.getMessage());
												insertBuilder.execute();
											}
										});
							}
						}
					}
				}
			});
		} finally {
			con.close();
		}
	}
}
