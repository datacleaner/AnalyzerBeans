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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.builder.SourceColumns;
import org.eobjects.analyzer.job.concurrent.ForkTaskListener;
import org.eobjects.analyzer.job.concurrent.JobCompletionTaskListener;
import org.eobjects.analyzer.job.concurrent.JoinTaskListener;
import org.eobjects.analyzer.job.concurrent.RunNextTaskTaskListener;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.AssignCallbacksAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CloseReferenceDataTask;
import org.eobjects.analyzer.job.tasks.CollectResultsAndCloseAnalyzerBeanTask;
import org.eobjects.analyzer.job.tasks.InitializeReferenceDataTask;
import org.eobjects.analyzer.job.tasks.RunExplorerTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.FilterBeanInstance;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.ReturnResultsCallback;
import org.eobjects.analyzer.lifecycle.RunExplorerCallback;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

/**
 * A delegate for the AnalysisRunner to put the state of a single job into.
 * 
 * As opposed to the AnalysisRunner, this class is NOT thread-safe (which is why
 * the AnalysisRunner instantiates a new delegate for each execution).
 * 
 * @author Kasper Sørensen
 */
final class AnalysisRunnerJobDelegate {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisRunnerJobDelegate.class);

	private final StorageProvider _storageProvider;
	private final AnalysisJob _job;
	private final TaskRunner _taskRunner;
	private final AnalysisListener _analysisListener;
	private final Queue<AnalyzerJobResult> _resultQueue;
	private final JobCompletionTaskListener _jobCompletionTaskListener;
	private final ErrorAware _errorAware;
	private final ArrayList<AnalyzerJob> _explorerJobs;
	private final ArrayList<AnalyzerJob> _rowProcessingJobs;
	private final Datastore _datastore;
	private final Collection<TransformerJob> _transformerJobs;
	private final Collection<FilterJob> _filterJobs;
	private final Collection<MergedOutcomeJob> _mergedOutcomeJobs;
	private final ReferenceDataActivationManager _rowProcessingReferenceDataActivationManager;

	private ReferenceDataActivationManager _explorerReferenceDataActivationManager;

	public AnalysisRunnerJobDelegate(AnalysisJob job, TaskRunner taskRunner, StorageProvider storageProvider,
			AnalysisListener analysisListener, Queue<AnalyzerJobResult> resultQueue, ErrorAware errorAware) {
		_storageProvider = storageProvider;
		_job = job;
		_taskRunner = taskRunner;
		_analysisListener = analysisListener;
		_resultQueue = resultQueue;

		// A task listener that will register either succesfull executions or
		// unexpected errors (which will be delegated to the errorListener)
		_jobCompletionTaskListener = new JobCompletionTaskListener(job, analysisListener, 2);
		_errorAware = errorAware;

		_datastore = _job.getDatastore();
		_transformerJobs = _job.getTransformerJobs();
		_filterJobs = _job.getFilterJobs();
		_mergedOutcomeJobs = _job.getMergedOutcomeJobs();

		Collection<AnalyzerJob> analyzerJobs = _job.getAnalyzerJobs();

		_explorerJobs = new ArrayList<AnalyzerJob>();
		_rowProcessingJobs = new ArrayList<AnalyzerJob>();
		for (AnalyzerJob analyzerJob : analyzerJobs) {
			AnalyzerBeanDescriptor<?> descriptor = analyzerJob.getDescriptor();
			if (descriptor.isExploringAnalyzer()) {
				_explorerJobs.add(analyzerJob);
			} else if (descriptor.isRowProcessingAnalyzer()) {
				_rowProcessingJobs.add(analyzerJob);
			} else {
				throw new IllegalStateException("AnalyzerBeanDescriptor is neither exploring nor row processing: "
						+ descriptor);
			}
		}

		_rowProcessingReferenceDataActivationManager = new ReferenceDataActivationManager();
		_explorerReferenceDataActivationManager = new ReferenceDataActivationManager();
	}

	public AnalysisResultFuture run() {
		_analysisListener.jobBegin(_job);

		validateSingleTableInput(_transformerJobs);
		validateSingleTableInput(_filterJobs);
		validateSingleTableInputForMergedOutcomes(_mergedOutcomeJobs);

		validateSingleTableInput(_rowProcessingJobs);

		// at this point we are done validating the job, it will run.
		scheduleExplorers();
		scheduleRowProcessing();

		return new AnalysisResultFutureImpl(_resultQueue, _jobCompletionTaskListener, _errorAware);
	}

	private void scheduleRowProcessing() {
		final Map<Table, RowProcessingPublisher> rowProcessingPublishers = new HashMap<Table, RowProcessingPublisher>();
		for (FilterJob filterJob : _filterJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, filterJob, _resultQueue, _analysisListener, _taskRunner);
		}

		for (MergedOutcomeJob mergedOutcomeJob : _mergedOutcomeJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, mergedOutcomeJob);
		}

		for (TransformerJob transformerJob : _transformerJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, transformerJob, _resultQueue, _analysisListener,
					_taskRunner);
		}
		for (AnalyzerJob analyzerJob : _rowProcessingJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, analyzerJob, _resultQueue, _analysisListener,
					_taskRunner);
		}

		logger.info("Created {} row processor publishers", rowProcessingPublishers.size());

		final TaskRunnable[] finalTasks = new TaskRunnable[2];
		finalTasks[0] = new TaskRunnable(null, _jobCompletionTaskListener);
		finalTasks[1] = new TaskRunnable(new CloseReferenceDataTask(_rowProcessingReferenceDataActivationManager), null);

		final TaskListener rowProcessorPublishersDoneCompletionListener = new JoinTaskListener(
				rowProcessingPublishers.size(), new ForkTaskListener("All row consumers finished", _taskRunner, finalTasks));

		for (RowProcessingPublisher rowProcessingPublisher : rowProcessingPublishers.values()) {
			List<TaskRunnable> initTasks = rowProcessingPublisher.createInitialTasks(_taskRunner, _resultQueue,
					rowProcessorPublishersDoneCompletionListener, _datastore);
			logger.debug("Scheduling {} tasks for row processing publisher: {}", initTasks.size(), rowProcessingPublisher);
			for (TaskRunnable taskRunnable : initTasks) {
				_taskRunner.run(taskRunnable);
			}
		}
	}

	private void scheduleExplorers() {
		final int numExplorerJobs = _explorerJobs.size();
		if (numExplorerJobs == 0) {
			_jobCompletionTaskListener.onComplete(null);
			return;
		}

		final TaskRunnable[] finalTasks = new TaskRunnable[2];
		finalTasks[0] = new TaskRunnable(null, _jobCompletionTaskListener);
		finalTasks[1] = new TaskRunnable(new CloseReferenceDataTask(_explorerReferenceDataActivationManager), null);

		final TaskListener explorersDoneTaskListener = new JoinTaskListener(numExplorerJobs, new ForkTaskListener(
				"Exploring analyzers done", _taskRunner, finalTasks));

		// begin explorer jobs first because they can run independently (
		for (AnalyzerJob explorerJob : _explorerJobs) {
			final DataContextProvider dataContextProvider = _datastore.getDataContextProvider();

			AnalyzerBeanInstance instance = new AnalyzerBeanInstance(explorerJob.getDescriptor());

			RunExplorerCallback runExplorerCallback = new RunExplorerCallback(_job, explorerJob, _datastore,
					_analysisListener);
			ReturnResultsCallback returnResultsCallback = new ReturnResultsCallback(_job, explorerJob, _resultQueue,
					_analysisListener);

			// set up scheduling for the explorers
			Task closeTask = new CollectResultsAndCloseAnalyzerBeanTask(instance, dataContextProvider);
			TaskListener runFinishedListener = new RunNextTaskTaskListener(_taskRunner, closeTask, explorersDoneTaskListener);
			Task runTask = new RunExplorerTask(instance);

			TaskListener referenceDataInitFinishedListener = new RunNextTaskTaskListener(_taskRunner, runTask,
					runFinishedListener);

			Task initializeReferenceData = new InitializeReferenceDataTask(_explorerReferenceDataActivationManager);
			RunNextTaskTaskListener joinFinishedListener = new RunNextTaskTaskListener(_taskRunner, initializeReferenceData,
					referenceDataInitFinishedListener);

			TaskListener initializeFinishedListener = new JoinTaskListener(numExplorerJobs, joinFinishedListener);

			AssignConfiguredCallback assignConfiguredCallback = new AssignConfiguredCallback(explorerJob.getConfiguration(),
					_explorerReferenceDataActivationManager);
			CloseCallback closeCallback = new CloseCallback();

			AssignCallbacksAndInitializeTask initTask = new AssignCallbacksAndInitializeTask(instance, _storageProvider,
					_storageProvider.createRowAnnotationFactory(), dataContextProvider, assignConfiguredCallback,
					new InitializeCallback(), runExplorerCallback, returnResultsCallback, closeCallback);

			// begin the explorers
			_taskRunner.run(initTask, initializeFinishedListener);
		}
	}

	private void validateSingleTableInputForMergedOutcomes(Collection<MergedOutcomeJob> mergedOutcomeJobs) {
		for (MergedOutcomeJob mergedOutcomeJob : mergedOutcomeJobs) {
			Table originatingTable = null;
			MergeInput[] input = mergedOutcomeJob.getMergeInputs();
			for (MergeInput mergeInput : input) {
				InputColumn<?>[] inputColumns = mergeInput.getInputColumns();
				for (InputColumn<?> inputColumn : inputColumns) {
					if (originatingTable == null) {
						originatingTable = findOriginatingTable(inputColumn);
					} else {
						if (!originatingTable.equals(findOriginatingTable(inputColumn))) {
							throw new IllegalArgumentException("Input columns in " + mergeInput
									+ " originate from different tables");
						}
					}
				}
			}
		}
	}

	/**
	 * Prevents that any row processing components have input from different
	 * tables.
	 * 
	 * @param beanJobs
	 */
	private void validateSingleTableInput(Collection<? extends ConfigurableBeanJob<?>> beanJobs) {
		for (ConfigurableBeanJob<?> beanJob : beanJobs) {
			InputColumn<?>[] input = beanJob.getInput();
			Table originatingTable = null;
			for (InputColumn<?> inputColumn : input) {
				if (originatingTable == null) {
					originatingTable = findOriginatingTable(inputColumn);
				} else {
					if (!originatingTable.equals(findOriginatingTable(inputColumn))) {
						throw new IllegalArgumentException("Input columns in " + beanJob
								+ " originate from different tables");
					}
				}
			}
		}
	}

	private Table findOriginatingTable(InputColumn<?> inputColumn) {
		SourceColumnFinder finder = new SourceColumnFinder();
		finder.addSources(_job.getTransformerJobs());
		finder.addSources(_job.getMergedOutcomeJobs());
		return finder.findOriginatingTable(inputColumn);
	}

	private void registerRowProcessingPublishers(Map<Table, RowProcessingPublisher> rowProcessingPublishers,
			MergedOutcomeJob mergedOutcomeJob) {

		Collection<RowProcessingPublisher> publishers = rowProcessingPublishers.values();
		for (RowProcessingPublisher rowProcessingPublisher : publishers) {
			boolean prerequisiteOutcomesExist = true;
			MergeInput[] mergeInputs = mergedOutcomeJob.getMergeInputs();
			for (MergeInput mergeInput : mergeInputs) {
				Outcome prerequisiteOutcome = mergeInput.getOutcome();
				if (!rowProcessingPublisher.containsOutcome(prerequisiteOutcome)) {
					prerequisiteOutcomesExist = false;
					break;
				}
			}

			if (prerequisiteOutcomesExist) {
				rowProcessingPublisher.addMergedOutcomeJob(mergedOutcomeJob);
			}
		}
	}

	private void registerRowProcessingPublishers(Map<Table, RowProcessingPublisher> rowProcessingPublishers,
			ConfigurableBeanJob<?> beanJob, Collection<AnalyzerJobResult> resultQueue, AnalysisListener listener,
			TaskRunner taskRunner) {
		InputColumn<?>[] inputColumns = beanJob.getInput();
		Set<Column> physicalColumns = new HashSet<Column>();
		for (InputColumn<?> inputColumn : inputColumns) {
			physicalColumns.addAll(findSourcePhysicalColumns(_job, inputColumn));
		}

		Column[] physicalColumnsArray = physicalColumns.toArray(new Column[physicalColumns.size()]);

		Table[] tables = MetaModelHelper.getTables(physicalColumnsArray);
		for (Table table : tables) {
			RowProcessingPublisher rowPublisher = rowProcessingPublishers.get(table);
			if (rowPublisher == null) {
				rowPublisher = new RowProcessingPublisher(_job, _storageProvider, table, taskRunner, listener,
						_rowProcessingReferenceDataActivationManager);
				rowProcessingPublishers.put(table, rowPublisher);
			}

			// register the physical columns needed by this job
			Column[] tableColumns = MetaModelHelper.getTableColumns(table, physicalColumnsArray);
			rowPublisher.addPhysicalColumns(tableColumns);

			// find which input columns (both physical or virtual) are needed by
			// this per-table instance
			InputColumn<?>[] localInputColumns = getLocalInputColumns(table, inputColumns, _job);

			if (beanJob instanceof AnalyzerJob) {
				AnalyzerJob analyzerJob = (AnalyzerJob) beanJob;
				AnalyzerBeanInstance analyzerBeanInstance = new AnalyzerBeanInstance(analyzerJob.getDescriptor());
				rowPublisher.addRowProcessingAnalyzerBean(analyzerBeanInstance, analyzerJob, localInputColumns);
			} else if (beanJob instanceof TransformerJob) {
				TransformerJob transformerJob = (TransformerJob) beanJob;
				TransformerBeanInstance transformerBeanInstance = new TransformerBeanInstance(transformerJob.getDescriptor());
				rowPublisher.addTransformerBean(transformerBeanInstance, transformerJob, localInputColumns);
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
			Set<Column> sourcePhysicalColumns = findSourcePhysicalColumns(analysisJob, inputColumn);
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
	private Set<Column> findSourcePhysicalColumns(AnalysisJob analysisJob, InputColumn<?> inputColumn) {
		SourceColumnFinder finder = new SourceColumnFinder();

		finder.addSources(new SourceColumns(analysisJob.getSourceColumns()));
		finder.addSources(analysisJob.getTransformerJobs());
		finder.addSources(analysisJob.getMergedOutcomeJobs());

		return finder.findOriginatingColumns(inputColumn);
	}
}
