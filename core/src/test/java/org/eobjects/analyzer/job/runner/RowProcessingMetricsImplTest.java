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
package org.eobjects.analyzer.job.runner;

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.NumberAnalyzer;
import org.eobjects.analyzer.beans.filter.EqualsFilter;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Ref;

public class RowProcessingMetricsImplTest extends TestCase {

	private JdbcDatastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
	private AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
			.replace(new DatastoreCatalogImpl(datastore));
	private AnalysisJob job;

	public void testGetExpectedRowCountNoFilter() throws Exception {
		AnalysisJobBuilder ajb = createAnalysisJobBuilder();

		job = ajb.toAnalysisJob();

		assertEquals(23, getExpectedRowCount());
	}

	private AnalysisJobBuilder createAnalysisJobBuilder() {
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
		ajb.setDatastore(datastore);
		ajb.addSourceColumns("PUBLIC.EMPLOYEES.EMPLOYEENUMBER");
		ajb.addAnalyzer(NumberAnalyzer.class).addInputColumns(ajb.getSourceColumns());
		return ajb;
	}

	public void testGetExpectedRowCountMaxRows() throws Exception {
		AnalysisJobBuilder ajb = createAnalysisJobBuilder();

		FilterJobBuilder<MaxRowsFilter, ValidationCategory> filter = ajb.addFilter(MaxRowsFilter.class);
		filter.getConfigurableBean().setMaxRows(10);
		ajb.setDefaultRequirement(filter.getOutcome(ValidationCategory.VALID));

		job = ajb.toAnalysisJob();

		assertEquals(10, getExpectedRowCount());
	}

	public void testGetExpectedRowCountEquals() throws Exception {
		AnalysisJobBuilder ajb = createAnalysisJobBuilder();

		FilterJobBuilder<EqualsFilter, ValidationCategory> filter = ajb.addFilter(EqualsFilter.class);
		filter.addInputColumns(ajb.getSourceColumns());
		filter.getConfigurableBean().setValues(new String[] { "1002", "1165" });

		ajb.setDefaultRequirement(filter.getOutcome(ValidationCategory.VALID));

		job = ajb.toAnalysisJob();

		assertEquals(2, getExpectedRowCount());
	}

//	public void testGetExpectedRowCountMultipleFilters() throws Exception {
//		AnalysisJobBuilder ajb = createAnalysisJobBuilder();
//
//		FilterJobBuilder<EqualsFilter, ValidationCategory> filter1 = ajb.addFilter(EqualsFilter.class);
//		filter1.addInputColumns(ajb.getSourceColumns());
//		filter1.getConfigurableBean().setValues(new String[] { "1002", "1165" });
//
//		FilterJobBuilder<MaxRowsFilter, ValidationCategory> filter2 = ajb.addFilter(MaxRowsFilter.class);
//		filter2.getConfigurableBean().setMaxRows(10);
//		filter2.setRequirement(filter1.getOutcome(ValidationCategory.INVALID));
//		ajb.setDefaultRequirement(filter2.getOutcome(ValidationCategory.VALID));
//
//		job = ajb.toAnalysisJob();
//
//		assertEquals(2, getExpectedRowCount());
//	}

	private int getExpectedRowCount() {
		AnalysisListener analysisListener = new InfoLoggingAnalysisListener();
		TaskRunner taskRunner = configuration.getTaskRunner();

		LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(configuration.getInjectionManagerFactory()
				.getInjectionManager(job), null);
		SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
		sourceColumnFinder.addSources(job);

		final RowProcessingPublishers publishers = new RowProcessingPublishers(job, analysisListener, taskRunner,
				lifeCycleHelper, sourceColumnFinder);
		final AnalysisJobMetrics analysisJobMetrics = new AnalysisJobMetricsImpl(job, publishers);
		final RowProcessingPublisher publisher = publishers.getRowProcessingPublisher(publishers.getTables()[0]);
		List<TaskRunnable> tasks = publisher.createInitialTasks(taskRunner, null, null, datastore, analysisJobMetrics);
		for (TaskRunnable taskRunnable : tasks) {
			taskRunner.run(taskRunnable);
		}

		Table table = null;
		AnalyzerJob[] analyzerJobs = null;
		Ref<Query> queryRef = new Ref<Query>() {
			@Override
			public Query get() {
				return publisher.getQuery();
			}
		};

		RowProcessingMetricsImpl metrics = new RowProcessingMetricsImpl(analysisJobMetrics, table, analyzerJobs, queryRef);

		return metrics.getExpectedRows();
	}
}
