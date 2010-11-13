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
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
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
import org.eobjects.analyzer.job.concurrent.JobTaskListener;
import org.eobjects.analyzer.job.concurrent.JobTaskListenerImpl;
import org.eobjects.analyzer.job.concurrent.NestedTaskListener;
import org.eobjects.analyzer.job.concurrent.RunNextTaskCompletionListener;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.AssignCallbacksAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CollectResultsAndCloseAnalyzerBeanTask;
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
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public final class AnalysisRunnerImpl implements AnalysisRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AnalyzerBeansConfiguration _configuration;
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
		_sharedAnalysisListeners = sharedAnalysisListeners;
	}

	@Override
	public AnalysisResultFuture run(final AnalysisJob job) {
		final Queue<AnalyzerJobResult> resultQueue = new LinkedBlockingQueue<AnalyzerJobResult>();

		// This analysis listener will keep track of all collected errors
		final ErrorAwareAnalysisListener errorListener = new ErrorAwareAnalysisListener();

		// This analysis listener is a composite for all other listeners
		final CompositeAnalysisListener analysisListener = new CompositeAnalysisListener(errorListener,
				_sharedAnalysisListeners);

		if (DebugLoggingAnalysisListener.isEnabled()) {
			// enable debug logging?
			analysisListener.addDelegate(new DebugLoggingAnalysisListener());
		} else if (InfoLoggingAnalysisListener.isEnabled()) {
			analysisListener.addDelegate(new InfoLoggingAnalysisListener());
		}

		// A task listener that will register either succesfull executions or
		// unexpected errors (which will be delegated to the errorListener)
		final JobTaskListener finalTaskListener = new JobTaskListenerImpl(job, analysisListener, 2);

		// set up the task runner that is aware of errors
		final TaskRunner taskRunner = new ErrorAwareTaskRunnerWrapper(errorListener, _configuration.getTaskRunner());

		analysisListener.jobBegin(job);

		// declare all the "constants" of the job as final variables
		final DataContextProvider dataContextProvider = job.getDataContextProvider();
		final Collection<TransformerJob> transformerJobs = job.getTransformerJobs();
		final Collection<FilterJob> filterJobs = job.getFilterJobs();
		final Collection<MergedOutcomeJob> mergedOutcomeJobs = job.getMergedOutcomeJobs();

		validateSingleTableInput(job, transformerJobs);
		validateSingleTableInput(job, filterJobs);
		validateSingleTableInputForMergedOutcomes(job, mergedOutcomeJobs);

		final Collection<AnalyzerJob> analyzerJobs = job.getAnalyzerJobs();

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
		validateSingleTableInput(job, rowProcessingJobs);

		final TaskListener explorersDoneTaskListener = new NestedTaskListener("exploring analyzers", explorerJobs.size(),
				finalTaskListener);

		// begin explorer jobs first because they can run independently (
		for (AnalyzerJob explorerJob : explorerJobs) {
			AnalyzerBeanInstance instance = new AnalyzerBeanInstance(explorerJob.getDescriptor());

			RunExplorerCallback runExplorerCallback = new RunExplorerCallback(job, explorerJob, dataContextProvider,
					analysisListener);
			ReturnResultsCallback returnResultsCallback = new ReturnResultsCallback(job, explorerJob, resultQueue,
					analysisListener);

			// set up scheduling for the explorers
			Task closeTask = new CollectResultsAndCloseAnalyzerBeanTask(instance);
			TaskListener runExplorerTaskListener = new RunNextTaskCompletionListener(taskRunner, closeTask,
					explorersDoneTaskListener);
			Task runTask = new RunExplorerTask(instance);
			TaskListener initExplorerTaskListener = new RunNextTaskCompletionListener(taskRunner, runTask,
					runExplorerTaskListener);
			Task initTask = new AssignCallbacksAndInitializeTask(instance, _configuration.getStorageProvider(),
					_configuration.getStorageProvider().createRowAnnotationFactory(), dataContextProvider,
					new AssignConfiguredCallback(explorerJob.getConfiguration()), new InitializeCallback(),
					runExplorerCallback, returnResultsCallback, new CloseCallback());

			// begin the explorers
			taskRunner.run(initTask, initExplorerTaskListener);
		}

		final Map<Table, RowProcessingPublisher> rowProcessingPublishers = new HashMap<Table, RowProcessingPublisher>();
		for (FilterJob filterJob : filterJobs) {
			registerRowProcessingPublishers(job, rowProcessingPublishers, filterJob, resultQueue, analysisListener,
					taskRunner);
		}

		for (MergedOutcomeJob mergedOutcomeJob : mergedOutcomeJobs) {
			registerRowProcessingPublishers(job, rowProcessingPublishers, mergedOutcomeJob);
		}

		for (TransformerJob transformerJob : transformerJobs) {
			registerRowProcessingPublishers(job, rowProcessingPublishers, transformerJob, resultQueue, analysisListener,
					taskRunner);
		}
		for (AnalyzerJob analyzerJob : rowProcessingJobs) {
			registerRowProcessingPublishers(job, rowProcessingPublishers, analyzerJob, resultQueue, analysisListener,
					taskRunner);
		}

		logger.info("Created {} row processor publishers", rowProcessingPublishers.size());

		final TaskListener rowProcessorPublishersDoneCompletionListener = new NestedTaskListener("row processor publishers",
				rowProcessingPublishers.size(), finalTaskListener);

		for (RowProcessingPublisher rowProcessingPublisher : rowProcessingPublishers.values()) {
			List<TaskRunnable> initTasks = rowProcessingPublisher.createInitialTasks(taskRunner, resultQueue,
					rowProcessorPublishersDoneCompletionListener);
			logger.debug("Scheduling {} tasks for row processing publisher: {}", initTasks.size(), rowProcessingPublisher);
			for (TaskRunnable taskRunnable : initTasks) {
				taskRunner.run(taskRunnable);
			}
		}

		return new AnalysisResultFutureImpl(resultQueue, finalTaskListener, errorListener);
	}

	private void validateSingleTableInputForMergedOutcomes(AnalysisJob job, Collection<MergedOutcomeJob> mergedOutcomeJobs) {
		for (MergedOutcomeJob mergedOutcomeJob : mergedOutcomeJobs) {
			Table originatingTable = null;
			MergeInput[] input = mergedOutcomeJob.getMergeInputs();
			for (MergeInput mergeInput : input) {
				InputColumn<?>[] inputColumns = mergeInput.getInputColumns();
				for (InputColumn<?> inputColumn : inputColumns) {
					if (originatingTable == null) {
						originatingTable = findOriginatingTable(job, inputColumn);
					} else {
						if (!originatingTable.equals(findOriginatingTable(job, inputColumn))) {
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
	private void validateSingleTableInput(AnalysisJob analysisJob, Collection<? extends ConfigurableBeanJob<?>> beanJobs) {
		for (ConfigurableBeanJob<?> beanJob : beanJobs) {
			InputColumn<?>[] input = beanJob.getInput();
			Table originatingTable = null;
			for (InputColumn<?> inputColumn : input) {
				if (originatingTable == null) {
					originatingTable = findOriginatingTable(analysisJob, inputColumn);
				} else {
					if (!originatingTable.equals(findOriginatingTable(analysisJob, inputColumn))) {
						throw new IllegalArgumentException("Input columns in " + beanJob
								+ " originate from different tables");
					}
				}
			}
		}
	}

	private Table findOriginatingTable(AnalysisJob analysisJob, InputColumn<?> inputColumn) {
		SourceColumnFinder finder = new SourceColumnFinder();
		finder.addSources(analysisJob.getTransformerJobs());
		finder.addSources(analysisJob.getMergedOutcomeJobs());
		return finder.findOriginatingTable(inputColumn);
	}

	private void registerRowProcessingPublishers(AnalysisJob analysisJob,
			Map<Table, RowProcessingPublisher> rowProcessingPublishers, MergedOutcomeJob mergedOutcomeJob) {

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

	private void registerRowProcessingPublishers(AnalysisJob analysisJob,
			Map<Table, RowProcessingPublisher> rowProcessingPublishers, ConfigurableBeanJob<?> beanJob,
			Collection<AnalyzerJobResult> resultQueue, AnalysisListener listener, TaskRunner taskRunner) {
		InputColumn<?>[] inputColumns = beanJob.getInput();
		Set<Column> physicalColumns = new HashSet<Column>();
		for (InputColumn<?> inputColumn : inputColumns) {
			physicalColumns.addAll(findSourcePhysicalColumns(analysisJob, inputColumn));
		}

		Column[] physicalColumnsArray = physicalColumns.toArray(new Column[physicalColumns.size()]);

		Table[] tables = MetaModelHelper.getTables(physicalColumnsArray);
		for (Table table : tables) {
			RowProcessingPublisher rowPublisher = rowProcessingPublishers.get(table);
			if (rowPublisher == null) {
				rowPublisher = new RowProcessingPublisher(analysisJob, _configuration.getStorageProvider(), table,
						taskRunner, listener);
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
