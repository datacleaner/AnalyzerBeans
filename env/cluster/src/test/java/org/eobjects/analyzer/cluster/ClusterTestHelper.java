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
package org.eobjects.analyzer.cluster;

import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.transform.ConcatenatorTransformer;
import org.eobjects.analyzer.beans.writers.InsertIntoTableAnalyzer;
import org.eobjects.analyzer.beans.writers.WriteBufferSizeOption;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.Schema;
import org.junit.Assert;

public class ClusterTestHelper {

    public static AnalyzerBeansConfiguration createConfiguration(String testName, boolean multiThreaded) {
        final JdbcDatastore csvDatastore = new JdbcDatastore("csv", "jdbc:h2:mem:" + testName, "org.h2.Driver", "SA",
                "", true);
        final UpdateableDatastoreConnection con = csvDatastore.openConnection();
        con.getUpdateableDataContext().executeUpdate(new UpdateScript() {
            @Override
            public void run(UpdateCallback callback) {
                Schema schema = callback.getDataContext().getDefaultSchema();
                if (schema.getTableByName("testtable") != null) {
                    return;
                }
                callback.createTable(schema, "testtable").withColumn("id").ofType(ColumnType.INTEGER).asPrimaryKey()
                        .withColumn("name").ofType(ColumnType.VARCHAR).execute();
            }
        });
        con.close();

        final Datastore databaseDatastore = TestHelper.createSampleDatabaseDatastore("orderdb");

        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(databaseDatastore, csvDatastore);
        final TaskRunner taskRunner;
        if (multiThreaded) {
            taskRunner = new MultiThreadedTaskRunner(20);
        } else {
            taskRunner = new SingleThreadedTaskRunner();
        }
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(true);
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(MaxRowsFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(InsertIntoTableAnalyzer.class));

        final AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl().replace(taskRunner)
                .replace(datastoreCatalog).replace(descriptorProvider);
        return configuration;
    }

    public static void runConcatAndInsertJob(AnalyzerBeansConfiguration configuration, ClusterManager clusterManager)
            throws Throwable {
        // build a job that concats names and inserts the concatenated names
        // into a file
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.CONTACTFIRSTNAME",
                "CUSTOMERS.CONTACTLASTNAME");

        // concatenate firstname + lastname
        final TransformerJobBuilder<ConcatenatorTransformer> concatenator = jobBuilder
                .addTransformer(ConcatenatorTransformer.class);
        concatenator.addInputColumn(jobBuilder.getSourceColumnByName("CONTACTFIRSTNAME"));
        concatenator.addInputColumn(jobBuilder.getSourceColumnByName("CONTACTLASTNAME"));
        concatenator.setConfiguredProperty("Separator", " ");

        // insert into CSV file
        final Datastore csvDatastore = configuration.getDatastoreCatalog().getDatastore("csv");
        final Datastore dbDatastore = configuration.getDatastoreCatalog().getDatastore("orderdb");
        final DatastoreConnection csvCon = csvDatastore.openConnection();
        final DatastoreConnection dbCon = dbDatastore.openConnection();
        try {
            Schema schema = csvCon.getDataContext().getDefaultSchema();
            final String schemaName = schema.getName();
            final String tableName = schema.getTable(0).getName();

            final AnalyzerJobBuilder<InsertIntoTableAnalyzer> insert = jobBuilder
                    .addAnalyzer(InsertIntoTableAnalyzer.class);
            insert.setConfiguredProperty("Datastore", csvDatastore);
            insert.addInputColumn(jobBuilder.getSourceColumnByName("CUSTOMERNUMBER"));
            insert.addInputColumn(concatenator.getOutputColumns().get(0));
            insert.setConfiguredProperty("Schema name", schemaName);
            insert.setConfiguredProperty("Table name", tableName);
            insert.setConfiguredProperty("Column names", new String[] { "id", "name" });
            insert.setConfiguredProperty("Buffer size", WriteBufferSizeOption.TINY);

            // build the job
            final AnalysisJob job = jobBuilder.toAnalysisJob();

            // run the job in a distributed fashion
            final DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, clusterManager);
            final AnalysisResultFuture resultFuture = runner.run(job);

            resultFuture.await();

            if (resultFuture.isErrornous()) {
                List<Throwable> errors = resultFuture.getErrors();
                throw errors.get(0);
            }

            // check that the file created has the same amount of records as the
            // CUSTOMER table of orderdb.
            DataSet ds1 = dbCon.getDataContext().query().from("CUSTOMERS").selectCount().execute();
            DataSet ds2 = csvCon.getDataContext().query().from(tableName).selectCount().execute();
            try {
                Assert.assertTrue(ds1.next());
                Assert.assertTrue(ds2.next());
                Assert.assertEquals(ds1.getRow().toString(), ds2.getRow().toString());
            } finally {
                ds1.close();
                ds2.close();
            }

            // await multiple times to ensure that second time isn't distorting
            // the result
            resultFuture.await();
            resultFuture.await();

            // check that the analysis result elements are there...
            final Map<ComponentJob, AnalyzerResult> resultMap = resultFuture.getResultMap();
            Assert.assertEquals(1, resultMap.size());
            Assert.assertEquals("{ImmutableAnalyzerJob[name=null,analyzer=Insert into table]=122 inserts executed}",
                    resultMap.toString());

        } finally {
            dbCon.close();
            csvCon.close();
        }
    }
}