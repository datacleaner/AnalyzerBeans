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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSourceJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.ForkTaskListener;
import org.eobjects.analyzer.job.concurrent.JoinTaskListener;
import org.eobjects.analyzer.job.concurrent.RunNextTaskTaskListener;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.AssignCallbacksAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CloseBeanTask;
import org.eobjects.analyzer.job.tasks.CollectResultsAndCloseAnalyzerBeanTask;
import org.eobjects.analyzer.job.tasks.ConsumeRowTask;
import org.eobjects.analyzer.job.tasks.InitializeReferenceDataTask;
import org.eobjects.analyzer.job.tasks.RunRowProcessingPublisherTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AnalyzerLifeCycleCallback;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.FilterBeanInstance;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.ReturnResultsCallback;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RowProcessingPublisher {

	private final static Logger logger = LoggerFactory.getLogger(RowProcessingPublisher.class);

	private final Set<Column> _physicalColumns = new HashSet<Column>();
	private final List<RowProcessingConsumer> _consumers = new ArrayList<RowProcessingConsumer>();
	private final AnalysisJob _job;
	private final StorageProvider _storageProvider;
	private final Table _table;
	private final TaskRunner _taskRunner;
	private final AnalysisListener _analysisListener;
	private final ReferenceDataActivationManager _referenceDataActivationManager;
	private final DatastoreCatalog _datastoreCatalog;
	private final ReferenceDataCatalog _referenceDataCatalog;

	public RowProcessingPublisher(AnalysisJob job, StorageProvider storageProvider, Table table, TaskRunner taskRunner,
			AnalysisListener analysisListener, DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
			ReferenceDataActivationManager referenceDataActivationManager) {
		if (job == null) {
			throw new IllegalArgumentException("AnalysisJob cannot be null");
		}
		if (storageProvider == null) {
			throw new IllegalArgumentException("CollectionProvider cannot be null");
		}
		if (table == null) {
			throw new IllegalArgumentException("Table cannot be null");
		}
		if (taskRunner == null) {
			throw new IllegalArgumentException("TaskRunner cannot be null");
		}
		if (analysisListener == null) {
			throw new IllegalArgumentException("AnalysisListener cannot be null");
		}
		_job = job;
		_storageProvider = storageProvider;
		_table = table;
		_taskRunner = taskRunner;
		_analysisListener = analysisListener;
		_datastoreCatalog = datastoreCatalog;
		_referenceDataCatalog = referenceDataCatalog;
		_referenceDataActivationManager = referenceDataActivationManager;
	}

	public void addPhysicalColumns(Column... columns) {
		for (Column column : columns) {
			if (!_table.equals(column.getTable())) {
				throw new IllegalArgumentException("Column does not pertain to the correct table. Expected table: " + _table
						+ ", actual table: " + column.getTable());
			}
			_physicalColumns.add(column);
		}
	}

	public void run() {
		for (RowProcessingConsumer rowProcessingConsumer : _consumers) {
			if (rowProcessingConsumer instanceof AnalyzerConsumer) {
				AnalyzerJob analyzerJob = ((AnalyzerConsumer) rowProcessingConsumer).getComponentJob();
				_analysisListener.analyzerBegin(_job, analyzerJob);
			}
		}

		final Datastore datastore = _job.getDatastore();
		final DataContextProvider dcp = datastore.getDataContextProvider();
		final DataContext dataContext = dcp.getDataContext();

		final Query finalQuery;
		final List<RowProcessingConsumer> finalConsumers;
		final Collection<? extends Outcome> availableOutcomes;
		{
			final Column[] columnArray = _physicalColumns.toArray(new Column[_physicalColumns.size()]);
			final Query baseQuery = dataContext.query().from(_table).select(columnArray).toQuery();

			logger.debug("Base query for row processing: {}", baseQuery);

			final RowProcessingConsumerSorter sorter = new RowProcessingConsumerSorter(_consumers);
			final List<RowProcessingConsumer> sortedConsumers = sorter.createProcessOrderedConsumerList();
			if (logger.isDebugEnabled()) {
				logger.debug("Row processing order ({} consumers):", sortedConsumers.size());
				int i = 1;
				for (RowProcessingConsumer rowProcessingConsumer : sortedConsumers) {
					logger.debug(" {}) {}", i, rowProcessingConsumer);
					i++;
				}
			}

			final RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore, sortedConsumers,
					baseQuery);
			if (optimizer.isOptimizable()) {
				finalQuery = optimizer.getOptimizedQuery();
				finalConsumers = optimizer.getOptimizedConsumers();
				availableOutcomes = optimizer.getOptimizedAvailableOutcomes();
				logger.info("Base query was optimizable to: {}, (maxrows={})", finalQuery, finalQuery.getMaxRows());
			} else {
				finalQuery = baseQuery;
				finalConsumers = sortedConsumers;
				availableOutcomes = Collections.emptyList();
			}
		}

		int expectedRows = -1;
		{
			final Query countQuery = finalQuery.clone();
			countQuery.setMaxRows(null);
			countQuery.getSelectClause().removeItems();
			countQuery.selectCount();
			countQuery.getSelectClause().getItem(0).setFunctionApproximationAllowed(true);
			final DataSet countDataSet = dataContext.executeQuery(countQuery);
			if (countDataSet.next()) {
				Number count = (Number) countDataSet.getRow().getValue(0);
				if (count != null) {
					expectedRows = count.intValue();
				}
			}
			Integer maxRows = finalQuery.getMaxRows();
			if (maxRows != null) {
				expectedRows = Math.min(expectedRows, maxRows.intValue());
			}
		}

		_analysisListener.rowProcessingBegin(_job, _table, expectedRows);

		// TODO: Needs to delegate errors downstream
		final RowConsumerTaskListener taskListener = new RowConsumerTaskListener(_job, _analysisListener);
		final AtomicInteger rowNumber = new AtomicInteger(0);
		final DataSet dataSet = dataContext.executeQuery(finalQuery);

		// represents the distinct count of rows as well as the number of tasks
		// to execute
		int numTasks = 0;

		while (dataSet.next()) {
			if (taskListener.isErrornous()) {
				break;
			}
			Row metaModelRow = dataSet.getRow();
			ConsumeRowTask task = new ConsumeRowTask(finalConsumers, _table, metaModelRow, rowNumber, _job,
					_analysisListener, availableOutcomes);
			_taskRunner.run(task, taskListener);
			numTasks++;
		}

		taskListener.awaitTasks(numTasks);

		dataSet.close();
		dcp.close();

		if (!taskListener.isErrornous()) {
			_analysisListener.rowProcessingSuccess(_job, _table);
		}
	}

	public void addRowProcessingAnalyzerBean(AnalyzerBeanInstance analyzerBeanInstance, AnalyzerJob analyzerJob,
			InputColumn<?>[] inputColumns) {
		addConsumer(new AnalyzerConsumer(_job, analyzerBeanInstance, analyzerJob, inputColumns, _analysisListener));
	}

	public void addTransformerBean(TransformerBeanInstance transformerBeanInstance, TransformerJob transformerJob,
			InputColumn<?>[] inputColumns) {
		addConsumer(new TransformerConsumer(_job, transformerBeanInstance, transformerJob, inputColumns, _analysisListener));
	}

	public void addFilterBean(FilterBeanInstance filterBeanInstance, FilterJob filterJob, InputColumn<?>[] inputColumns) {
		addConsumer(new FilterConsumer(_job, filterBeanInstance, filterJob, inputColumns, _analysisListener));
	}

	public void addMergedOutcomeJob(MergedOutcomeJob mergedOutcomeJob) {
		addConsumer(new MergedOutcomeConsumer(mergedOutcomeJob));
	}

	public boolean containsOutcome(Outcome prerequisiteOutcome) {
		for (RowProcessingConsumer consumer : _consumers) {
			ComponentJob componentJob = consumer.getComponentJob();
			if (componentJob instanceof OutcomeSourceJob) {
				Outcome[] outcomes = ((OutcomeSourceJob) componentJob).getOutcomes();
				for (Outcome outcome : outcomes) {
					if (outcome.satisfiesRequirement(prerequisiteOutcome)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void addConsumer(RowProcessingConsumer consumer) {
		_consumers.add(consumer);
	}

	public List<TaskRunnable> createInitialTasks(TaskRunner taskRunner, Queue<AnalyzerJobResult> resultQueue,
			TaskListener rowProcessorPublishersTaskListener, Datastore datastore) {

		final List<RowProcessingConsumer> configurableConsumers = CollectionUtils.filter(_consumers,
				new Predicate<RowProcessingConsumer>() {
					@Override
					public Boolean eval(RowProcessingConsumer input) {
						return input.getComponentJob() instanceof ConfigurableBeanJob<?>;
					}
				});
		int numConfigurableConsumers = configurableConsumers.size();

		final TaskListener closeTaskListener = new JoinTaskListener(numConfigurableConsumers,
				rowProcessorPublishersTaskListener);

		final List<TaskRunnable> closeTasks = new ArrayList<TaskRunnable>(numConfigurableConsumers);
		for (RowProcessingConsumer consumer : configurableConsumers) {
			Task closeTask = createCloseTask(consumer, resultQueue);
			closeTasks.add(new TaskRunnable(closeTask, closeTaskListener));
		}

		final TaskListener runCompletionListener = new ForkTaskListener("run row processing", taskRunner, closeTasks);

		final RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this);

		final TaskListener referenceDataInitFinishedListener = new ForkTaskListener("Initialize row consumers", taskRunner,
				new TaskRunnable(runTask, runCompletionListener));

		final InitializeCallback initializeCallback = new InitializeCallback(_datastoreCatalog, _referenceDataCatalog);

		final RunNextTaskTaskListener joinFinishedListener = new RunNextTaskTaskListener(_taskRunner,
				new InitializeReferenceDataTask(_datastoreCatalog, _referenceDataCatalog, _referenceDataActivationManager),
				referenceDataInitFinishedListener);
		final TaskListener initFinishedListener = new JoinTaskListener(numConfigurableConsumers, joinFinishedListener);

		// Ticket #459: The RowAnnotationFactory should be shared by all
		// components within the same RowProcessingPublisher
		final RowAnnotationFactory rowAnnotationFactory = _storageProvider.createRowAnnotationFactory();

		final List<TaskRunnable> initTasks = new ArrayList<TaskRunnable>(numConfigurableConsumers);
		for (RowProcessingConsumer consumer : configurableConsumers) {
			initTasks.add(createInitTask(consumer, rowAnnotationFactory, initFinishedListener, resultQueue, datastore,
					initializeCallback));
		}
		return initTasks;
	}

	private Task createCloseTask(RowProcessingConsumer consumer, Queue<AnalyzerJobResult> resultQueue) {
		if (consumer instanceof TransformerConsumer || consumer instanceof FilterConsumer) {
			return new CloseBeanTask(consumer.getBeanInstance());
		} else if (consumer instanceof AnalyzerConsumer) {
			return new CollectResultsAndCloseAnalyzerBeanTask(((AnalyzerBeanInstance) consumer.getBeanInstance()));
		} else {
			throw new IllegalStateException("Unknown consumer type: " + consumer);
		}
	}

	private TaskRunnable createInitTask(RowProcessingConsumer consumer, RowAnnotationFactory rowAnnotationFactory,
			TaskListener listener, Queue<AnalyzerJobResult> resultQueue, Datastore datastore,
			InitializeCallback initializeCallback) {
		ComponentJob componentJob = consumer.getComponentJob();
		BeanConfiguration configuration = ((ConfigurableBeanJob<?>) componentJob).getConfiguration();

		AssignConfiguredCallback assignConfiguredCallback = new AssignConfiguredCallback(configuration,
				_referenceDataActivationManager);
		CloseCallback closeCallback = new CloseCallback();

		Task task;
		if (consumer instanceof TransformerConsumer) {
			TransformerConsumer transformerConsumer = (TransformerConsumer) consumer;
			TransformerBeanInstance transformerBeanInstance = transformerConsumer.getBeanInstance();

			task = new AssignCallbacksAndInitializeTask(transformerBeanInstance, _storageProvider, rowAnnotationFactory,
					assignConfiguredCallback, initializeCallback, closeCallback);
		} else if (consumer instanceof FilterConsumer) {
			FilterConsumer filterConsumer = (FilterConsumer) consumer;
			FilterBeanInstance filterBeanInstance = filterConsumer.getBeanInstance();

			task = new AssignCallbacksAndInitializeTask(filterBeanInstance, _storageProvider, rowAnnotationFactory,
					assignConfiguredCallback, initializeCallback, closeCallback);
		} else if (consumer instanceof AnalyzerConsumer) {
			AnalyzerConsumer analyzerConsumer = (AnalyzerConsumer) consumer;
			AnalyzerBeanInstance analyzerBeanInstance = analyzerConsumer.getBeanInstance();
			AnalyzerLifeCycleCallback returnResultsCallback = new ReturnResultsCallback(_job,
					analyzerConsumer.getComponentJob(), resultQueue, _analysisListener);

			task = new AssignCallbacksAndInitializeTask(analyzerBeanInstance, _storageProvider, rowAnnotationFactory, null,
					assignConfiguredCallback, initializeCallback, null, returnResultsCallback, closeCallback);
		} else {
			throw new IllegalStateException("Unknown consumer type: " + consumer);
		}
		return new TaskRunnable(task, listener);
	}

	@Override
	public String toString() {
		return "RowProcessingPublisher[table=" + _table.getQualifiedLabel() + ", consumers=" + _consumers.size() + "]";
	}
}
