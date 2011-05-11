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
package org.eobjects.analyzer.test.full.scenarios;

import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

public class CancellationAndMultiThreadingTest extends TestCase {

	public void test10Times() throws Exception {
		Thread[] threads = new Thread[10];
		for (int i = 0; i < threads.length; i++) {
			Thread thread = new Thread() {
				public void run() {
					runScenario();
				};
			};
			thread.start();
			threads[i] = thread;
		}

		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
	}

	public void runScenario() {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
				.addAnalyzerClass(ValueDistributionAnalyzer.class);
		StorageProvider storageProvider = TestHelper.createStorageProvider();

		MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(30);
		ThreadPoolExecutor executorService = (ThreadPoolExecutor) taskRunner.getExecutorService();
		assertEquals(30, executorService.getMaximumPoolSize());
		assertEquals(0, executorService.getActiveCount());

		DatastoreCatalog datastoreCatalog = TestHelper.createDatastoreCatalog();
		ReferenceDataCatalog referenceDataCatalog = TestHelper.createReferenceDataCatalog();

		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog,
				referenceDataCatalog, descriptorProvider, taskRunner, storageProvider);

		AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

		JdbcDatastore ds = TestHelper.createSampleDatabaseDatastore("foobar");
		DataContextProvider dcp = ds.getDataContextProvider();

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
		analysisJobBuilder.setDatastore(ds);

		Table table = dcp.getDataContext().getDefaultSchema().getTableByName("ORDERFACT");
		assertNotNull(table);

		Column statusColumn = table.getColumnByName("STATUS");
		Column commentsColumn = table.getColumnByName("COMMENTS");

		analysisJobBuilder.addSourceColumns(statusColumn, commentsColumn);
		analysisJobBuilder.addRowProcessingAnalyzer(ValueDistributionAnalyzer.class).addInputColumns(
				analysisJobBuilder.getSourceColumns());

		AnalysisJob job = analysisJobBuilder.toAnalysisJob();

		AnalysisResultFuture resultFuture = runner.run(job);

		try {
			Thread.sleep(550);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interrupted! " + e.getMessage());
		}

		resultFuture.cancel();

		assertFalse(resultFuture.isSuccessful());
		assertTrue(resultFuture.isCancelled());
		assertTrue(resultFuture.isErrornous());

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interrupted! " + e.getMessage());
		}

		assertEquals(30, executorService.getMaximumPoolSize());

		long completedTaskCount = executorService.getCompletedTaskCount();
		assertTrue("completedTaskCount was: " + completedTaskCount, completedTaskCount > 3);

		int largestPoolSize = executorService.getLargestPoolSize();
		assertTrue("largestPoolSize was: " + largestPoolSize, largestPoolSize > 5);
		assertEquals(0, executorService.getActiveCount());

		assertTrue(ds.isDataContextProviderOpen());

		dcp.close();
		analysisJobBuilder.close();

		assertFalse(ds.isDataContextProviderOpen());
		
		taskRunner.shutdown();
	}
}
