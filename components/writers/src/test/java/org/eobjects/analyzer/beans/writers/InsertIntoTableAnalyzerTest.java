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
import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.create.TableCreationBuilder;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;

public class InsertIntoTableAnalyzerTest extends TestCase {

	private JdbcDatastore jdbcDatastore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (jdbcDatastore == null) {
			jdbcDatastore = new JdbcDatastore("my datastore",
					"jdbc:hsqldb:mem:testErrorHandlingOption",
					"org.hsqldb.jdbcDriver");
			final UpdateableDatastoreConnection con = jdbcDatastore
					.openConnection();
			final UpdateableDataContext dc = con.getUpdateableDataContext();
			if (dc.getDefaultSchema().getTableByName("test_table") == null) {
				dc.executeUpdate(new UpdateScript() {
					@Override
					public void run(UpdateCallback cb) {
						cb.createTable(dc.getDefaultSchema(), "test_table")
								.withColumn("foo").ofType(ColumnType.VARCHAR)
								.withColumn("bar").ofType(ColumnType.INTEGER)
								.execute();
					}
				});
			}
			con.close();
		}
	}

	public void testErrorHandlingToInvalidFile() throws Exception {
		final InsertIntoTableAnalyzer insertIntoTable = new InsertIntoTableAnalyzer();
		insertIntoTable.datastore = jdbcDatastore;
		insertIntoTable.tableName = "test_table";
		insertIntoTable.columnNames = new String[] { "foo", "bar" };
		insertIntoTable.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;
		insertIntoTable.errorLogFile = new File(
				"src/test/resources/invalid-error-handling-file.csv");

		InputColumn<Object> col1 = new MockInputColumn<Object>("in1",
				Object.class);
		InputColumn<Object> col2 = new MockInputColumn<Object>("in2",
				Object.class);

		insertIntoTable.values = new InputColumn[] { col1, col2 };

		try {
			insertIntoTable.init();
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals(
					"Error log file does not have required column header: foo",
					e.getMessage());
		}
	}

	public void testErrorHandlingToAppendingFile() throws Exception {
		final File file = new File("target/valid-error-handling-file.csv");
		FileHelper.copy(new File(
				"src/test/resources/valid-error-handling-file.csv"), file);

		final InsertIntoTableAnalyzer insertIntoTable = new InsertIntoTableAnalyzer();
		insertIntoTable.datastore = jdbcDatastore;
		insertIntoTable.tableName = "test_table";
		insertIntoTable.columnNames = new String[] { "foo", "bar" };
		insertIntoTable.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;
		insertIntoTable.errorLogFile = file;

		InputColumn<Object> col1 = new MockInputColumn<Object>("in1",
				Object.class);
		InputColumn<Object> col2 = new MockInputColumn<Object>("in2",
				Object.class);

		insertIntoTable.values = new InputColumn[] { col1, col2 };

		insertIntoTable.init();

		insertIntoTable.run(
				new MockInputRow().put(col1, "blabla").put(col2, "hello int"),
				2);

		WriteDataResult result = insertIntoTable.getResult();
		assertEquals(0, result.getWrittenRowCount());
		assertEquals(2, result.getErrorRowCount());

		assertEquals(
				"foo,bar,extra1,insert_into_table_error_message,extra2[newline]"
						+ "f,b,e1,m,e2[newline]"
						+ "\"blabla\",\"hello int\",\"\",\"Could not convert hello int to number\",\"\"[newline]"
						+ "\"blabla\",\"hello int\",\"\",\"Could not convert hello int to number\",\"\"",
				FileHelper.readFileAsString(file).replaceAll("\n",
						"\\[newline\\]"));
	}

	public void testErrorHandlingToTempFile() throws Exception {
		final InsertIntoTableAnalyzer insertIntoTable = new InsertIntoTableAnalyzer();
		insertIntoTable.datastore = jdbcDatastore;
		insertIntoTable.tableName = "test_table";
		insertIntoTable.columnNames = new String[] { "foo", "bar" };
		insertIntoTable.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;

		InputColumn<Object> col1 = new MockInputColumn<Object>("in1",
				Object.class);
		InputColumn<Object> col2 = new MockInputColumn<Object>("in2",
				Object.class);

		insertIntoTable.values = new InputColumn[] { col1, col2 };

		insertIntoTable.init();

		// a valid row
		insertIntoTable.run(
				new MockInputRow().put(col1, "hello world").put(col2, 123), 1);

		// null values - should be accepted
		insertIntoTable.run(new MockInputRow().put(col1, null).put(col2, null),
				1);

		// invalid types, they should be automatically converted
		insertIntoTable.run(new MockInputRow().put(col1, 123).put(col2, "123"),
				1);

		// invalid and unconvertable types!
		insertIntoTable.run(new MockInputRow().put(col2,
				"hey I am a string in a number field"), 1);

		// another valid row (after the failing one)
		insertIntoTable.run(
				new MockInputRow().put(col1, "foo bar").put(col2, 3123), 1);

		WriteDataResult result = insertIntoTable.getResult();

		// assertions about succes rows
		assertEquals(4, result.getWrittenRowCount());
		assertEquals(jdbcDatastore, result.getDatastore(null));
		Table table = result.getPreviewTable(jdbcDatastore);
		assertEquals("TEST_TABLE", table.getName());

		// make assertions about error rows
		assertEquals(1, result.getErrorRowCount());
		FileDatastore errorDatastore = result.getErrorDatastore();
		assertNotNull(errorDatastore);

		DatastoreConnection errorCon = errorDatastore.openConnection();
		Schema errorSchema = errorCon.getDataContext().getDefaultSchema();
		assertEquals(1, errorSchema.getTableCount());
		Table errorTable = errorSchema.getTables()[0];
		assertEquals("[foo, bar, insert_into_table_error_message]",
				Arrays.toString(errorTable.getColumnNames()));
		DataSet ds = errorCon.getDataContext().query().from(errorTable)
				.select("foo").and("bar")
				.and("insert_into_table_error_message").execute();
		assertTrue(ds.next());
		assertEquals(
				"Row[values=[, hey I am a string in a number field, "
						+ "Could not convert hey I am a string in a number field to number]]",
				ds.getRow().toString());
		assertFalse(ds.next());
		errorCon.close();

		String filename = errorDatastore.getFilename();
		assertEquals("4 records written to table\n"
				+ " - WARNING! 1 record failed, written to file: " + filename,
				result.toString());
	}

	public void testMultiThreadedRunNoColumnNames() throws Exception {
		final CsvDatastore datastoreIn = new CsvDatastore("in",
				"src/test/resources/datastorewriter-in.csv");
		final CsvDatastore datastoreOut = new CsvDatastore("out",
				"target/datastorewriter-out.csv");

		if (new File(datastoreOut.getFilename()).exists()) {
			assertTrue("Could not delete output file",
					new File(datastoreOut.getFilename()).delete());
		}

		// count input lines and get columns
		final Column[] columns;
		final Number countIn;
		{
			DatastoreConnection con = datastoreIn.openConnection();
			Table table = con.getDataContext().getDefaultSchema().getTables()[0];

			columns = table.getColumns();

			DataSet ds = con.getDataContext().query().from(table).selectCount()
					.execute();
			assertTrue(ds.next());
			countIn = (Number) ds.getRow().getValue(0);
			assertFalse(ds.next());
			ds.close();

			con.close();
		}

		// create output file
		{
			DatastoreConnection con = datastoreOut.openConnection();
			final UpdateableDataContext dc = (UpdateableDataContext) con
					.getDataContext();
			dc.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					TableCreationBuilder createTableBuilder = callback
							.createTable(dc.getDefaultSchema(), "mytable");
					for (Column column : columns) {
						createTableBuilder = createTableBuilder.withColumn(
								column.getName()).ofType(column.getType());
					}
					createTableBuilder.execute();
				}
			});
			con.close();
		}

		// run a "copy lines" job with multithreading
		{
			DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(
					datastoreIn);

			AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
					.replace(new MultiThreadedTaskRunner(4)).replace(
							datastoreCatalog);

			AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
			ajb.setDatastore(datastoreIn);

			ajb.addSourceColumns(columns);

			AnalyzerJobBuilder<InsertIntoTableAnalyzer> analyzerJobBuilder = ajb
					.addAnalyzer(InsertIntoTableAnalyzer.class);
			analyzerJobBuilder.addInputColumns(ajb.getSourceColumns());
			analyzerJobBuilder.setConfiguredProperty("Datastore", datastoreOut);
			analyzerJobBuilder.setConfiguredProperty("Column names",
					"col0,col1,col2,col3,col4".split(","));

			assertTrue(analyzerJobBuilder.isConfigured());

			AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
			AnalysisResultFuture resultFuture = runner.run(ajb.toAnalysisJob());

			assertTrue(resultFuture.isSuccessful());
		}

		// count output file lines
		final Number countOut;
		{
			DatastoreConnection con = datastoreOut.openConnection();
			DataSet ds = con
					.getDataContext()
					.query()
					.from(con.getDataContext().getDefaultSchema().getTables()[0])
					.selectCount().execute();
			assertTrue(ds.next());
			countOut = (Number) ds.getRow().getValue(0);
			assertFalse(ds.next());
			ds.close();
			con.close();
		}

		assertEquals(countIn, countOut);
	}
}
