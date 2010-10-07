package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.BeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.job.concurrent.JobCompletionListener;
import org.eobjects.analyzer.job.concurrent.NestedCompletionListener;
import org.eobjects.analyzer.job.concurrent.RunNextTaskCompletionListener;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.AssignCallbacksAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CollectResultsAndCloseAnalyzerBeanTask;
import org.eobjects.analyzer.job.tasks.RunExplorerTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.FilterBeanInstance;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.ReturnResultsCallback;
import org.eobjects.analyzer.lifecycle.RunExplorerCallback;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public final class AnalysisRunnerImpl implements AnalysisRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AnalyzerBeansConfiguration _configuration;
	private final TaskRunner _taskRunner;
	private final CollectionProvider _collectionProvider;
	private final AnalysisListener[] _sharedAnalysisListeners;

	/**
	 * Creates an AnalysisRunner based on a configuration, with no listeners
	 * 
	 * @param configuration
	 */
	public AnalysisRunnerImpl(AnalyzerBeansConfiguration configuration) {
		this(configuration, new AnalysisListener[0]);
	}

	/**
	 * Create an AnalysisRunner with a set of listeners, based on a
	 * configuration
	 * 
	 * @param configuration
	 * @param sharedAnalysisListeners
	 */
	public AnalysisRunnerImpl(AnalyzerBeansConfiguration configuration, AnalysisListener... sharedAnalysisListeners) {
		if (configuration == null) {
			throw new IllegalArgumentException("configuration cannot be null");
		}
		_configuration = configuration;
		_taskRunner = _configuration.getTaskRunner();
		_collectionProvider = _configuration.getCollectionProvider();
		_sharedAnalysisListeners = sharedAnalysisListeners;
	}

	@Override
	public AnalysisResultFuture run(final AnalysisJob job) {

		// A completion listener that simply waits for two onComplete() calls.
		// One for the explorers, one for the row processor publishers
		final JobCompletionListener finalCompletionListener = new JobCompletionListener(job, _sharedAnalysisListeners, 2);

		final Queue<AnalyzerResult> resultQueue = new LinkedBlockingQueue<AnalyzerResult>();
		final AnalysisResultFutureImpl analysisResultFuture = new AnalysisResultFutureImpl(resultQueue,
				finalCompletionListener);

		final AnalysisCancellationListener cancellationListener = new AnalysisCancellationListener(analysisResultFuture);
		final AnalysisListener listener = new CompositeAnalysisListener(cancellationListener, _sharedAnalysisListeners);
		final ErrorReporterFactory errorReporterFactory = new ErrorReporterFactoryImpl(listener);

		listener.jobBegin(job);

		// declare all the "constants" of the job as final variables
		final DataContextProvider dataContextProvider = job.getDataContextProvider();
		final Collection<FilterJob> filterJobs = job.getFilterJobs();
		final Collection<TransformerJob> transformerJobs = job.getTransformerJobs();
		final Collection<AnalyzerJob> analyzerJobs = job.getAnalyzerJobs();

		final List<AnalyzerBeanInstance> analyzerBeanInstances = new ArrayList<AnalyzerBeanInstance>();
		final List<TransformerBeanInstance> transformerBeanInstances = new ArrayList<TransformerBeanInstance>();

		final List<AnalyzerJob> explorerJobs = new ArrayList<AnalyzerJob>();
		final List<AnalyzerJob> rowProcessingJobs = new ArrayList<AnalyzerJob>();
		for (AnalyzerJob analyzerJob : analyzerJobs) {
			AnalyzerBeanDescriptor<?> descriptor = analyzerJob.getDescriptor();
			if (descriptor.isExploringAnalyzer()) {
				explorerJobs.add(analyzerJob);
			} else if (descriptor.isRowProcessingAnalyzer()) {
				rowProcessingJobs.add(analyzerJob);
			} else {
				throw new IllegalStateException("AnalyzerBeanDescriptor is neither exploring nor row processing: "
						+ descriptor);
			}
		}

		final CompletionListener explorersDoneCompletionListener = new NestedCompletionListener("exploring analyzers",
				explorerJobs.size(), finalCompletionListener);

		// begin explorer jobs first because they can run independently (
		for (AnalyzerJob explorerJob : explorerJobs) {
			AnalyzerBeanInstance instance = new AnalyzerBeanInstance(explorerJob.getDescriptor());
			analyzerBeanInstances.add(instance);

			RunExplorerCallback runExplorerCallback = new RunExplorerCallback(job, explorerJob, dataContextProvider,
					listener);
			ReturnResultsCallback returnResultsCallback = new ReturnResultsCallback(job, explorerJob, resultQueue, listener);

			// set up scheduling for the explorers
			Task closeTask = new CollectResultsAndCloseAnalyzerBeanTask(explorersDoneCompletionListener, instance);
			CompletionListener runExplorerCompletionListener = new RunNextTaskCompletionListener(_taskRunner, closeTask,
					errorReporterFactory.unknownErrorReporter(job));
			Task runTask = new RunExplorerTask(instance, runExplorerCompletionListener);
			CompletionListener initExplorerCompletionListener = new RunNextTaskCompletionListener(_taskRunner, runTask,
					errorReporterFactory.analyzerErrorReporter(job, explorerJob));
			Task initTask = new AssignCallbacksAndInitializeTask(initExplorerCompletionListener, instance,
					_collectionProvider, dataContextProvider, new AssignConfiguredCallback(explorerJob.getConfiguration()),
					new InitializeCallback(), runExplorerCallback, returnResultsCallback, new CloseCallback());

			// begin the explorers
			_taskRunner.run(initTask, errorReporterFactory.unknownErrorReporter(job));
		}

		final Map<Table, RowProcessingPublisher> rowProcessingPublishers = new HashMap<Table, RowProcessingPublisher>();
		for (FilterJob filterJob : filterJobs) {
			registerRowProcessingPublishers(job, rowProcessingPublishers, filterJob, null, transformerBeanInstances,
					resultQueue, listener, errorReporterFactory);
		}
		for (TransformerJob transformerJob : transformerJobs) {
			registerRowProcessingPublishers(job, rowProcessingPublishers, transformerJob, null, transformerBeanInstances,
					resultQueue, listener, errorReporterFactory);
		}
		for (AnalyzerJob analyzerJob : rowProcessingJobs) {
			registerRowProcessingPublishers(job, rowProcessingPublishers, analyzerJob, analyzerBeanInstances, null,
					resultQueue, listener, errorReporterFactory);
		}

		logger.info("Created {} row processor publishers", rowProcessingPublishers.size());

		final CompletionListener rowProcessorPublishersDoneCompletionListener = new NestedCompletionListener(
				"row processor publishers", rowProcessingPublishers.size(), finalCompletionListener);

		for (RowProcessingPublisher rowProcessingPublisher : rowProcessingPublishers.values()) {
			List<Task> initTasks = rowProcessingPublisher.createInitialTasks(_taskRunner, resultQueue,
					rowProcessorPublishersDoneCompletionListener);
			logger.debug("Scheduling {} tasks for row processing publisher: {}", initTasks.size(), rowProcessingPublisher);
			for (Task task : initTasks) {
				_taskRunner.run(task, errorReporterFactory.unknownErrorReporter(job));
			}
		}

		return analysisResultFuture;
	}

	private void registerRowProcessingPublishers(AnalysisJob analysisJob,
			Map<Table, RowProcessingPublisher> rowProcessingPublishers, BeanJob<?> beanJob,
			List<AnalyzerBeanInstance> analyzerBeanInstances, List<TransformerBeanInstance> transformerBeanInstances,
			Collection<AnalyzerResult> resultQueue, AnalysisListener listener, ErrorReporterFactory errorReporterFactory) {
		InputColumn<?>[] inputColumns = beanJob.getInput();
		Set<Column> physicalColumns = new HashSet<Column>();
		for (InputColumn<?> inputColumn : inputColumns) {
			physicalColumns.addAll(getSourcePhysicalColumns(analysisJob, inputColumn));
		}

		Column[] physicalColumnsArray = physicalColumns.toArray(new Column[physicalColumns.size()]);

		Table[] tables = MetaModelHelper.getTables(physicalColumnsArray);
		for (Table table : tables) {
			RowProcessingPublisher rowPublisher = rowProcessingPublishers.get(table);
			if (rowPublisher == null) {
				rowPublisher = new RowProcessingPublisher(analysisJob, _collectionProvider, table, listener,
						errorReporterFactory);
				rowProcessingPublishers.put(table, rowPublisher);
			}

			// register the physical columns needed by this job
			Column[] tableColumns = MetaModelHelper.getTableColumns(table, physicalColumnsArray);
			rowPublisher.addPhysicalColumns(tableColumns);

			// find which input columns (both physical or virtual) are needed by
			// this per-table instance
			InputColumn<?>[] localInputColumns = getLocalInputColumns(table, inputColumns, analysisJob);

			if (beanJob instanceof AnalyzerJob) {
				AnalyzerJob analyzerJob = (AnalyzerJob) beanJob;
				AnalyzerBeanInstance analyzerBeanInstance = new AnalyzerBeanInstance(analyzerJob.getDescriptor());
				rowPublisher.addRowProcessingAnalyzerBean(analyzerBeanInstance, analyzerJob, localInputColumns);

				analyzerBeanInstances.add(analyzerBeanInstance);
			} else if (beanJob instanceof TransformerJob) {
				TransformerJob transformerJob = (TransformerJob) beanJob;
				TransformerBeanInstance transformerBeanInstance = new TransformerBeanInstance(transformerJob.getDescriptor());
				rowPublisher.addTransformerBean(transformerBeanInstance, transformerJob, localInputColumns);

				transformerBeanInstances.add(transformerBeanInstance);
			} else if (beanJob instanceof FilterJob) {
				FilterJob filterJob = (FilterJob) beanJob;
				FilterBeanInstance filterBeanInstance = new FilterBeanInstance(filterJob.getDescriptor());
				rowPublisher.addFilterBean(filterBeanInstance, filterJob, localInputColumns);
			} else {
				throw new UnsupportedOperationException("Unsupported job type: " + beanJob);
			}
		}
	}

	private InputColumn<?>[] getLocalInputColumns(Table table, InputColumn<?>[] inputColumns, AnalysisJob analysisJob) {
		if (table == null || inputColumns == null || inputColumns.length == 0) {
			return new InputColumn<?>[0];
		}
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (InputColumn<?> inputColumn : inputColumns) {
			Set<Column> sourcePhysicalColumns = getSourcePhysicalColumns(analysisJob, inputColumn);
			for (Column physicalColumn : sourcePhysicalColumns) {
				if (table.equals(physicalColumn.getTable())) {
					result.add(inputColumn);
					break;
				}
			}
		}
		return result.toArray(new InputColumn<?>[result.size()]);
	}

	// helper method for recursively finding all physical columns by traversing
	// the transformers input and output
	private Set<Column> getSourcePhysicalColumns(AnalysisJob analysisJob, InputColumn<?> inputColumn) {
		Set<Column> physicalColumns = new HashSet<Column>();
		if (inputColumn.isPhysicalColumn()) {
			physicalColumns.add(inputColumn.getPhysicalColumn());
		} else {
			Collection<TransformerJob> transformerJobs = analysisJob.getTransformerJobs();
			boolean found = false;
			for (TransformerJob transformerJob : transformerJobs) {
				MutableInputColumn<?>[] output = transformerJob.getOutput();
				for (MutableInputColumn<?> outputColumn : output) {
					if (inputColumn.equals(outputColumn)) {
						found = true;
						InputColumn<?>[] input = transformerJob.getInput();
						for (InputColumn<?> transformerInputColumn : input) {
							physicalColumns.addAll(getSourcePhysicalColumns(analysisJob, transformerInputColumn));
						}
					}
				}
			}
			if (!found) {
				throw new IllegalStateException("Could not find source physical column for: " + inputColumn);
			}
		}
		return physicalColumns;
	}
}
