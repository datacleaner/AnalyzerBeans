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

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
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
import org.eobjects.metamodel.schema.Table;

public class InsertIntoTableAnalyzerTest extends TestCase {

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
