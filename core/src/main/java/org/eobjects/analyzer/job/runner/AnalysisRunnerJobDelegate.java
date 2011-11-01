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

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.ForkTaskListener;
import org.eobjects.analyzer.job.concurrent.JobCompletionTaskListener;
import org.eobjects.analyzer.job.concurrent.JoinTaskListener;
import org.eobjects.analyzer.job.concurrent.RunNextTaskTaskListener;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.AssignCallbacksAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CloseBeanTaskListener;
import org.eobjects.analyzer.job.tasks.CloseReferenceDataTaskListener;
import org.eobjects.analyzer.job.tasks.CloseResourcesTaskListener;
import org.eobjects.analyzer.job.tasks.CollectResultsTask;
import org.eobjects.analyzer.job.tasks.InitializeReferenceDataTask;
import org.eobjects.analyzer.job.tasks.RunExplorerTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.BeanInstance;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.metamodel.MetaModelHelper;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final AnalysisJob _job;
	private final AnalyzerBeansConfiguration _configuration;
	private final TaskRunner _taskRunner;
	private final AnalysisListener _analysisListener;
	private final Queue<JobAndResult> _resultQueue;
	private final JobCompletionTaskListener _jobCompletionTaskListener;
	private final ErrorAware _errorAware;
	private final Datastore _datastore;
	private final Collection<ExplorerJob> _explorerJobs;
	private final Collection<AnalyzerJob> _analyzerJobs;
	private final Collection<TransformerJob> _transformerJobs;
	private final Collection<FilterJob> _filterJobs;
	private final Collection<MergedOutcomeJob> _mergedOutcomeJobs;
	private final ReferenceDataActivationManager _rowProcessingReferenceDataActivationManager;
	private final SourceColumnFinder _sourceColumnFinder;
	private final ReferenceDataActivationManager _explorerReferenceDataActivationManager;

	public AnalysisRunnerJobDelegate(AnalysisJob job, AnalyzerBeansConfiguration configuration, TaskRunner taskRunner,
			AnalysisListener analysisListener, Queue<JobAndResult> resultQueue, ErrorAware errorAware) {
		_job = job;
		_configuration = configuration;
		_taskRunner = taskRunner;
		_analysisListener = analysisListener;
		_resultQueue = resultQueue;

		_sourceColumnFinder = new SourceColumnFinder();
		_sourceColumnFinder.addSources(_job);

		// A task listener that will register either succesfull executions or
		// unexpected errors (which will be delegated to the errorListener)
		_jobCompletionTaskListener = new JobCompletionTaskListener(job, analysisListener, 2);
		_errorAware = errorAware;

		_datastore = _job.getDatastore();
		_transformerJobs = _job.getTransformerJobs();
		_filterJobs = _job.getFilterJobs();
		_mergedOutcomeJobs = _job.getMergedOutcomeJobs();

		_explorerJobs = _job.getExplorerJobs();
		_analyzerJobs = _job.getAnalyzerJobs();

		_rowProcessingReferenceDataActivationManager = new ReferenceDataActivationManager();
		_explorerReferenceDataActivationManager = new ReferenceDataActivationManager();
	}

	/**
	 * Runs the job
	 * 
	 * @return
	 */
	public AnalysisResultFuture run() {
		_analysisListener.jobBegin(_job);

		validateSingleTableInput(_transformerJobs);
		validateSingleTableInput(_filterJobs);
		validateSingleTableInputForMergedOutcomes(_mergedOutcomeJobs);

		validateSingleTableInput(_analyzerJobs);

		// the injection manager is job scoped
		final InjectionManager injectionManager = _configuration.getInjectionManagerFactory().getInjectionManager(_job);

		// at this point we are done validating the job, it will run.
		scheduleExplorers(injectionManager);
		scheduleRowProcessing(injectionManager);

		return new AnalysisResultFutureImpl(_resultQueue, _jobCompletionTaskListener, _errorAware);
	}

	/**
	 * Starts row processing job flows.
	 * 
	 * @param injectionManager
	 */
	private void scheduleRowProcessing(InjectionManager injectionManager) {
		final Map<Table, RowProcessingPublisher> rowProcessingPublishers = new HashMap<Table, RowProcessingPublisher>();
		for (FilterJob filterJob : _filterJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, filterJob, _analysisListener, _taskRunner,
					injectionManager);
		}

		for (MergedOutcomeJob mergedOutcomeJob : _mergedOutcomeJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, mergedOutcomeJob);
		}

		for (TransformerJob transformerJob : _transformerJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, transformerJob, _analysisListener, _taskRunner,
					injectionManager);
		}
		for (AnalyzerJob analyzerJob : _analyzerJobs) {
			registerRowProcessingPublishers(rowProcessingPublishers, analyzerJob, _analysisListener, _taskRunner,
					injectionManager);
		}

		logger.info("Created {} row processor publishers", rowProcessingPublishers.size());

		final List<TaskRunnable> finalTasks = new ArrayList<TaskRunnable>(2);
		finalTasks.add(new TaskRunnable(null, _jobCompletionTaskListener));
		finalTasks.add(new TaskRunnable(null, new CloseReferenceDataTaskListener(
				_rowProcessingReferenceDataActivationManager)));

		final ForkTaskListener finalTaskListener = new ForkTaskListener("All row consumers finished", _taskRunner,
				finalTasks);
		
		final TaskListener rowProcessorPublishersDoneCompletionListener = new JoinTaskListener(
				rowProcessingPublishers.size(), finalTaskListener);

		for (RowProcessingPublisher rowProcessingPublisher : rowProcessingPublishers.values()) {
			List<TaskRunnable> initTasks = rowProcessingPublisher.createInitialTasks(_taskRunner, _resultQueue,
					rowProcessorPublishersDoneCompletionListener, _datastore);
			logger.debug("Scheduling {} tasks for row processing publisher: {}", initTasks.size(), rowProcessingPublisher);
			for (TaskRunnable taskRunnable : initTasks) {
				_taskRunner.run(taskRunnable);
			}
		}
	}

	/**
	 * Starts exploration based job flows.
	 * 
	 * @param injectionManager
	 */
	private void scheduleExplorers(final InjectionManager injectionManager) {
		final int numExplorerJobs = _explorerJobs.size();
		if (numExplorerJobs == 0) {
			_jobCompletionTaskListener.onComplete(null);
			return;
		}

		final List<TaskRunnable> finalTasks = new ArrayList<TaskRunnable>();
		finalTasks.add(new TaskRunnable(null, _jobCompletionTaskListener));
		finalTasks.add(new TaskRunnable(null, new CloseReferenceDataTaskListener(_explorerReferenceDataActivationManager)));

		final TaskListener explorersDoneTaskListener = new JoinTaskListener(numExplorerJobs, new ForkTaskListener(
				"Exploring analyzers done", _taskRunner, finalTasks));

		final InitializeCallback initializeCallback = new InitializeCallback(injectionManager);

		// begin explorer jobs first because they can run independently (
		for (ExplorerJob explorerJob : _explorerJobs) {
			final DatastoreConnection dataContextProvider = _datastore.openConnection();
			final BeanInstance<? extends Explorer<?>> beanInstance = BeanInstance.create(explorerJob.getDescriptor());

			finalTasks.add(new TaskRunnable(null, new CloseResourcesTaskListener(dataContextProvider)));
			finalTasks.add(new TaskRunnable(null, new CloseBeanTaskListener(beanInstance)));

			// set up scheduling for the explorers
			Task closeTask = new CollectResultsTask(beanInstance, _job, explorerJob, _resultQueue, _analysisListener);
			TaskListener runFinishedListener = new RunNextTaskTaskListener(_taskRunner, closeTask, explorersDoneTaskListener);
			Task runTask = new RunExplorerTask(beanInstance, _job, explorerJob, _datastore, _analysisListener);

			TaskListener referenceDataInitFinishedListener = new RunNextTaskTaskListener(_taskRunner, runTask,
					runFinishedListener);

			Task initializeReferenceData = new InitializeReferenceDataTask(injectionManager,
					_explorerReferenceDataActivationManager);
			RunNextTaskTaskListener joinFinishedListener = new RunNextTaskTaskListener(_taskRunner, initializeReferenceData,
					referenceDataInitFinishedListener);

			TaskListener initializeFinishedListener = new JoinTaskListener(numExplorerJobs, joinFinishedListener);

			AssignConfiguredCallback assignConfiguredCallback = new AssignConfiguredCallback(explorerJob.getConfiguration(),
					_explorerReferenceDataActivationManager);
			CloseCallback closeCallback = new CloseCallback();

			AssignCallbacksAndInitializeTask initTask = new AssignCallbacksAndInitializeTask(beanInstance, injectionManager,
					assignConfiguredCallback, initializeCallback, closeCallback);

			// begin the explorers
			_taskRunner.run(initTask, initializeFinishedListener);
		}
	}

	private void validateSingleTableInputForMergedOutcomes(Collection<MergedOutcomeJob> mergedOutcomeJobs) {
		Table originatingTable = null;

		for (MergedOutcomeJob mergedOutcomeJob : mergedOutcomeJobs) {
			MergeInput[] input = mergedOutcomeJob.getMergeInputs();
			for (MergeInput mergeInput : input) {
				InputColumn<?>[] inputColumns = mergeInput.getInputColumns();
				for (InputColumn<?> inputColumn : inputColumns) {
					Table currentTable = _sourceColumnFinder.findOriginatingTable(inputColumn);
					if (currentTable != null) {
						if (originatingTable == null) {
							originatingTable = currentTable;
						} else {
							if (!originatingTable.equals(currentTable)) {
								throw new IllegalArgumentException("Input columns in " + mergeInput
										+ " originate from different tables");
							}
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
			Table originatingTable = null;
			InputColumn<?>[] input = beanJob.getInput();

			for (InputColumn<?> inputColumn : input) {
				Table table = _sourceColumnFinder.findOriginatingTable(inputColumn);
				if (table != null) {
					if (originatingTable == null) {
						originatingTable = table;
					} else {
						if (!originatingTable.equals(table)) {
							throw new IllegalArgumentException("Input columns in " + beanJob
									+ " originate from different tables");
						}
					}
				}
			}
		}

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
			ConfigurableBeanJob<?> beanJob, AnalysisListener listener, TaskRunner taskRunner,
			InjectionManager injectionManager) {
		final Set<Column> physicalColumns = new HashSet<Column>();

		final InputColumn<?>[] inputColumns = beanJob.getInput();
		for (InputColumn<?> inputColumn : inputColumns) {
			physicalColumns.addAll(_sourceColumnFinder.findOriginatingColumns(inputColumn));
		}
		final Outcome[] requirements = beanJob.getRequirements();
		for (Outcome requirement : requirements) {
			physicalColumns.addAll(_sourceColumnFinder.findOriginatingColumns(requirement));
		}

		final Column[] physicalColumnsArray = physicalColumns.toArray(new Column[physicalColumns.size()]);
		final Table[] tables;
		if (physicalColumns.isEmpty()) {
			// if not dependent on any specific tables, make component available
			// for all tables
			Set<Table> allTables = new HashSet<Table>();
			Collection<InputColumn<?>> allSourceColumns = _job.getSourceColumns();
			for (InputColumn<?> inputColumn : allSourceColumns) {
				allTables.add(inputColumn.getPhysicalColumn().getTable());
			}
			tables = allTables.toArray(new Table[allTables.size()]);
		} else {
			tables = MetaModelHelper.getTables(physicalColumnsArray);
		}

		for (Table table : tables) {
			RowProcessingPublisher rowPublisher = rowProcessingPublishers.get(table);
			if (rowPublisher == null) {
				rowPublisher = new RowProcessingPublisher(_job, table, taskRunner, listener, injectionManager,
						_rowProcessingReferenceDataActivationManager);
				rowProcessingPublishers.put(table, rowPublisher);
			}

			// register the physical columns needed by this job
			Column[] tableColumns = MetaModelHelper.getTableColumns(table, physicalColumnsArray);
			rowPublisher.addPhysicalColumns(tableColumns);

			// find which input columns (both physical or virtual) are needed by
			// this per-table instance
			InputColumn<?>[] localInputColumns = getLocalInputColumns(table, inputColumns);

			if (beanJob instanceof AnalyzerJob) {
				AnalyzerJob analyzerJob = (AnalyzerJob) beanJob;
				BeanInstance<? extends Analyzer<?>> beanInstance = BeanInstance.create(analyzerJob.getDescriptor());
				rowPublisher.addRowProcessingAnalyzerBean(beanInstance, analyzerJob, localInputColumns);
			} else if (beanJob instanceof TransformerJob) {
				TransformerJob transformerJob = (TransformerJob) beanJob;
				BeanInstance<? extends Transformer<?>> beanInstance = BeanInstance.create(transformerJob.getDescriptor());
				rowPublisher.addTransformerBean(beanInstance, transformerJob, localInputColumns);
			} else if (beanJob instanceof FilterJob) {
				FilterJob filterJob = (FilterJob) beanJob;
				BeanInstance<? extends Filter<?>> beanInstance = BeanInstance.create(filterJob.getDescriptor());
				rowPublisher.addFilterBean(beanInstance, filterJob, localInputColumns);
			} else {
				throw new UnsupportedOperationException("Unsupported job type: " + beanJob);
			}
		}
	}

	private InputColumn<?>[] getLocalInputColumns(Table table, InputColumn<?>[] inputColumns) {
		if (table == null || inputColumns == null || inputColumns.length == 0) {
			return new InputColumn<?>[0];
		}
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (InputColumn<?> inputColumn : inputColumns) {
			Set<Column> sourcePhysicalColumns = _sourceColumnFinder.findOriginatingColumns(inputColumn);
			for (Column physicalColumn : sourcePhysicalColumns) {
				if (table.equals(physicalColumn.getTable())) {
					result.add(inputColumn);
					break;
				}
			}
		}
		return result.toArray(new InputColumn<?>[result.size()]);
	}
}
