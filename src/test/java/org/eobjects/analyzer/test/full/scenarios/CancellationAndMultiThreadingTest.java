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

import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
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
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.MetaModelTestCase;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

public class CancellationAndMultiThreadingTest extends MetaModelTestCase {

	public void testScenario() throws Throwable {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
				"org.eobjects.analyzer.beans", true);
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

		DataContext dc = DataContextFactory.createJdbcDataContext(getTestDbConnection());

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
		analysisJobBuilder.setDataContextProvider(new SingleDataContextProvider(dc, new JdbcDatastore("foobar", dc)));

		Table table = dc.getDefaultSchema().getTableByName("ORDERFACT");
		assertNotNull(table);

		Column statusColumn = table.getColumnByName("STATUS");
		Column commentsColumn = table.getColumnByName("COMMENTS");

		analysisJobBuilder.addSourceColumns(statusColumn, commentsColumn);
		analysisJobBuilder.addRowProcessingAnalyzer(ValueDistributionAnalyzer.class).addInputColumns(
				analysisJobBuilder.getSourceColumns());

		AnalysisJob job = analysisJobBuilder.toAnalysisJob();

		AnalysisResultFuture resultFuture = runner.run(job);

		Thread.sleep(100);

		resultFuture.cancel();

		assertFalse(resultFuture.isSuccessful());
		assertTrue(resultFuture.isCancelled());
		assertTrue(resultFuture.isErrornous());

		Thread.sleep(100);

		assertEquals(30, executorService.getMaximumPoolSize());

		long completedTaskCount = executorService.getCompletedTaskCount();
		assertTrue("completedTaskCount was: " + completedTaskCount, completedTaskCount > 10);

		int largestPoolSize = executorService.getLargestPoolSize();
		assertTrue("largestPoolSize was: " + largestPoolSize, largestPoolSize > 10);
		assertEquals(0, executorService.getActiveCount());
	}
}
