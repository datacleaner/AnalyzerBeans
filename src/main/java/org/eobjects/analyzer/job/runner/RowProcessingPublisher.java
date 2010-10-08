package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.job.concurrent.CompletionListenerAwareErrorReporterWrapper;
import org.eobjects.analyzer.job.concurrent.ErrorReporter;
import org.eobjects.analyzer.job.concurrent.NestedCompletionListener;
import org.eobjects.analyzer.job.concurrent.ScheduleTasksCompletionListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.AssignCallbacksAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CloseBeanTask;
import org.eobjects.analyzer.job.tasks.CollectResultsAndCloseAnalyzerBeanTask;
import org.eobjects.analyzer.job.tasks.ConsumeRowTask;
import org.eobjects.analyzer.job.tasks.ConsumeRowTaskCompletionListener;
import org.eobjects.analyzer.job.tasks.RunRowProcessingPublisherTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AnalyzerLifeCycleCallback;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.FilterBeanInstance;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.LifeCycleCallback;
import org.eobjects.analyzer.lifecycle.ReturnResultsCallback;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public final class RowProcessingPublisher {

	private final static Logger logger = LoggerFactory.getLogger(RowProcessingPublisher.class);

	private final Set<Column> _physicalColumns = new HashSet<Column>();
	private final List<RowProcessingConsumer> _consumers = new ArrayList<RowProcessingConsumer>();
	private final AnalysisJob _job;
	private final CollectionProvider _collectionProvider;
	private final Table _table;
	private final TaskRunner _taskRunner;
	private final ErrorReporterFactory _errorReporterFactory;
	private final AnalysisListener _analysisListener;

	public RowProcessingPublisher(AnalysisJob job, CollectionProvider collectionProvider, Table table,
			TaskRunner taskRunner, AnalysisListener analysisListener, ErrorReporterFactory errorReporterFactory) {
		if (job == null) {
			throw new IllegalArgumentException("AnalysisJob cannot be null");
		}
		if (collectionProvider == null) {
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
		if (errorReporterFactory == null) {
			throw new IllegalArgumentException("ErrorReporterFactory cannot be null");
		}
		_job = job;
		_collectionProvider = collectionProvider;
		_table = table;
		_taskRunner = taskRunner;
		_analysisListener = analysisListener;
		_errorReporterFactory = errorReporterFactory;
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
				AnalyzerJob analyzerJob = ((AnalyzerConsumer) rowProcessingConsumer).getBeanJob();
				_analysisListener.analyzerBegin(_job, analyzerJob);
			}
		}

		final DataContext dataContext = _job.getDataContextProvider().getDataContext();

		int expectedRows = -1;
		Query countQuery = dataContext.query().from(_table).selectCount().toQuery();
		DataSet countDataSet = dataContext.executeQuery(countQuery);
		if (countDataSet.next()) {
			Number count = (Number) countDataSet.getRow().getValue(0);
			if (count != null) {
				expectedRows = count.intValue();
			}
		}

		_analysisListener.rowProcessingBegin(_job, _table, expectedRows);

		final Column[] columnArray = _physicalColumns.toArray(new Column[_physicalColumns.size()]);

		final Query q = dataContext.query().from(_table).select(columnArray).toQuery();

		SelectItem countAllItem = null;
		if (useGroupByOptimization()) {
			logger.info("Using GROUP BY optimization");
			q.groupBy(columnArray);
			countAllItem = SelectItem.getCountAllItem();
			q.select(countAllItem);
		}

		logger.debug("Employing query for row processing: {}", q);

		final Iterable<RowProcessingConsumer> consumers = createProcessOrderedConsumerList(_consumers);
		if (logger.isDebugEnabled()) {
			logger.debug("Row processing order ({} consumers):", _consumers.size());
			int i = 1;
			for (RowProcessingConsumer rowProcessingConsumer : consumers) {
				logger.debug(" {}) {}", i, rowProcessingConsumer);
				i++;
			}
		}

		ConsumeRowTaskCompletionListener completionListener = new ConsumeRowTaskCompletionListener();

		final ErrorReporter errorReporter = new CompletionListenerAwareErrorReporterWrapper(completionListener,
				_errorReporterFactory.unknownErrorReporter(_job));

		int taskCount = 0;
		final AtomicInteger rowNumber = new AtomicInteger(0);
		final DataSet dataSet = dataContext.executeQuery(q);
		while (dataSet.next()) {
			Row metaModelRow = dataSet.getRow();
			ConsumeRowTask task = new ConsumeRowTask(consumers, _table, metaModelRow, countAllItem, rowNumber, _job,
					_analysisListener, completionListener);
			_taskRunner.run(task, errorReporter);
			taskCount++;
		}

		completionListener.awaitCount(taskCount);

		dataSet.close();
		_analysisListener.rowProcessingSuccess(_job, _table);
	}

	private boolean useGroupByOptimization() {
		if (_physicalColumns.size() > 10) {
			logger.info("Skipping GROUP BY optimization because of the high column amount");
			return false;
		}
		Datastore datastore = _job.getDataContextProvider().getDatastore();
		if (datastore == null) {
			logger.info("Skipping GROUP BY optimization because no datastore is attached to the DataContextProvider");
			return false;
		}
		if (datastore instanceof CsvDatastore) {
			logger.info("Skipping GROUP BY optimization because the Datastore is based on a CSV file");
			return false;
		}
		return true;
	}

	protected static List<RowProcessingConsumer> createProcessOrderedConsumerList(
			Collection<? extends RowProcessingConsumer> consumers) {
		List<RowProcessingConsumer> result = new LinkedList<RowProcessingConsumer>();

		Collection<RowProcessingConsumer> remainingConsumers = new LinkedList<RowProcessingConsumer>(consumers);
		Set<InputColumn<?>> availableVirtualColumns = new HashSet<InputColumn<?>>();
		Set<FilterJob> addedFilterJobs = new HashSet<FilterJob>();

		while (!remainingConsumers.isEmpty()) {
			boolean changed = false;
			for (Iterator<RowProcessingConsumer> it = remainingConsumers.iterator(); it.hasNext();) {
				RowProcessingConsumer consumer = it.next();

				boolean accepted = true;

				// make sure that any dependent filter outcome is evaluated
				// before this component
				FilterOutcome requirement = consumer.getBeanJob().getRequirement();
				if (requirement != null) {
					FilterJob filterJob = requirement.getFilterJob();
					if (!addedFilterJobs.contains(filterJob)) {
						accepted = false;
					}
				}

				// make sure that all the required colums are present
				if (accepted) {
					InputColumn<?>[] requiredInput = consumer.getRequiredInput();
					for (InputColumn<?> inputColumn : requiredInput) {
						if (!inputColumn.isPhysicalColumn()) {
							if (!availableVirtualColumns.contains(inputColumn)) {
								accepted = false;
								break;
							}
						}
					}
				}

				if (accepted) {
					result.add(consumer);
					it.remove();
					changed = true;
					if (consumer instanceof TransformerConsumer) {
						TransformerConsumer transformerConsumer = (TransformerConsumer) consumer;
						MutableInputColumn<?>[] virtualColumns = transformerConsumer.getBeanJob().getOutput();
						for (MutableInputColumn<?> virtualColumn : virtualColumns) {
							availableVirtualColumns.add(virtualColumn);
						}
					}
					if (consumer instanceof FilterConsumer) {
						addedFilterJobs.add((FilterJob) consumer.getBeanJob());
					}
				}
			}

			if (!changed) {
				// should never happen, but if a bug enters the
				// algorithm this exception will quickly expose it
				throw new IllegalStateException("Could not detect next consumer in processing order");
			}
		}

		return result;
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

	private void addConsumer(RowProcessingConsumer consumer) {
		_consumers.add(consumer);
	}

	public List<Task> createInitialTasks(TaskRunner taskRunner, Queue<AnalyzerResult> resultQueue,
			CompletionListener rowProcessorPublishersDoneCompletionListener) {
		int numConsumers = _consumers.size();

		CompletionListener closeCompletionListener = new NestedCompletionListener("row processor consumers", numConsumers,
				rowProcessorPublishersDoneCompletionListener);

		List<TaskRunnable> closeTasks = new ArrayList<TaskRunnable>(numConsumers);
		for (RowProcessingConsumer consumer : _consumers) {
			Task closeTask = createCloseTask(consumer, resultQueue, closeCompletionListener);
			closeTasks.add(new TaskRunnable(closeTask, _errorReporterFactory.unknownErrorReporter(_job)));
		}

		CompletionListener runCompletionListener = new ScheduleTasksCompletionListener("run row processing", taskRunner, 1,
				closeTasks);

		Collection<TaskRunnable> runTasksToSchedule = new ArrayList<TaskRunnable>(1);
		RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this, runCompletionListener);
		runTasksToSchedule.add(new TaskRunnable(runTask, _errorReporterFactory.unknownErrorReporter(_job)));

		CompletionListener initCompletionListener = new ScheduleTasksCompletionListener("initialize row consumers",
				taskRunner, numConsumers, runTasksToSchedule);

		List<Task> initTasks = new ArrayList<Task>(numConsumers);
		for (RowProcessingConsumer consumer : _consumers) {
			initTasks.add(createInitTask(consumer, initCompletionListener, resultQueue));
		}
		return initTasks;
	}

	private Task createCloseTask(RowProcessingConsumer consumer, Queue<AnalyzerResult> resultQueue,
			CompletionListener completionListener) {
		if (consumer instanceof TransformerConsumer || consumer instanceof FilterConsumer) {
			return new CloseBeanTask(completionListener, consumer.getBeanInstance());
		} else if (consumer instanceof AnalyzerConsumer) {
			return new CollectResultsAndCloseAnalyzerBeanTask(completionListener,
					((AnalyzerBeanInstance) consumer.getBeanInstance()));
		} else {
			throw new IllegalStateException("Unknown consumer type: " + consumer);
		}
	}

	private Task createInitTask(RowProcessingConsumer consumer, CompletionListener completionListener,
			Queue<AnalyzerResult> resultQueue) {
		LifeCycleCallback assignConfiguredCallback = new AssignConfiguredCallback(consumer.getBeanJob().getConfiguration());
		LifeCycleCallback initializeCallback = new InitializeCallback();
		LifeCycleCallback closeCallback = new CloseCallback();

		DataContextProvider dataContextProvider = _job.getDataContextProvider();

		if (consumer instanceof TransformerConsumer) {
			TransformerConsumer transformerConsumer = (TransformerConsumer) consumer;
			TransformerBeanInstance transformerBeanInstance = transformerConsumer.getBeanInstance();

			return new AssignCallbacksAndInitializeTask(completionListener, transformerBeanInstance, _collectionProvider,
					dataContextProvider, assignConfiguredCallback, initializeCallback, closeCallback);
		} else if (consumer instanceof FilterConsumer) {
			FilterConsumer filterConsumer = (FilterConsumer) consumer;
			FilterBeanInstance filterBeanInstance = filterConsumer.getBeanInstance();

			return new AssignCallbacksAndInitializeTask(completionListener, filterBeanInstance, _collectionProvider,
					dataContextProvider, assignConfiguredCallback, initializeCallback, closeCallback);
		} else if (consumer instanceof AnalyzerConsumer) {
			AnalyzerConsumer analyzerConsumer = (AnalyzerConsumer) consumer;
			AnalyzerBeanInstance analyzerBeanInstance = analyzerConsumer.getBeanInstance();
			AnalyzerLifeCycleCallback returnResultsCallback = new ReturnResultsCallback(_job, analyzerConsumer.getBeanJob(),
					resultQueue, _analysisListener);

			return new AssignCallbacksAndInitializeTask(completionListener, analyzerBeanInstance, _collectionProvider,
					dataContextProvider, assignConfiguredCallback, initializeCallback, null, returnResultsCallback,
					closeCallback);
		} else {
			throw new IllegalStateException("Unknown consumer type: " + consumer);
		}
	}

	@Override
	public String toString() {
		return "RowProcessingPublisher[table=" + _table.getQualifiedLabel() + ", consumers=" + _consumers.size() + "]";
	}
}
