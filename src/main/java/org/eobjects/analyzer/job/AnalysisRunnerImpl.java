package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.JobListDescriptorProvider;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.job.concurrent.ScheduleTasksCompletionListener;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.concurrent.WaitableCompletionListener;
import org.eobjects.analyzer.job.tasks.AssignAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CollectResultsAndCloseAnalyzerBeanTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.AssignConfiguredRowProcessingCallback;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.ReturnResultsCallback;
import org.eobjects.analyzer.lifecycle.RunExplorerCallback;
import org.eobjects.analyzer.lifecycle.RunRowProcessorsCallback;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class AnalysisRunnerImpl implements AnalysisRunner {

	private static final Logger logger = LoggerFactory
			.getLogger(AnalysisRunnerImpl.class);

	private AnalyzerBeansConfiguration _configuration;
	private Integer _rowProcessorAnalyzersCount;

	public AnalysisRunnerImpl(AnalyzerBeansConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException("configuration cannot be null");
		}
		_configuration = configuration;
	}

	@Override
	public AnalysisResultFuture run(DataContextProvider dataContextProvider,
			SimpleAnalyzerJob firstJob, SimpleAnalyzerJob... additionalJobs) {
		ArrayList<SimpleAnalyzerJob> jobs = new ArrayList<SimpleAnalyzerJob>(
				additionalJobs.length + 1);
		jobs.add(firstJob);
		for (int i = 0; i < additionalJobs.length; i++) {
			jobs.add(additionalJobs[i]);
		}
		return run(dataContextProvider, jobs);
	}

	@Override
	public AnalysisResultFuture run(DataContextProvider dataContextProvider,
			Collection<? extends SimpleAnalyzerJob> jobs) {
		if (logger.isInfoEnabled()) {
			logger.info("run(...) invoked.");
			logger.info("jobs: " + jobs.size());
			for (SimpleAnalyzerJob job : jobs) {
				logger.info(" - " + job);
			}
		}

		final Queue<AnalyzerResult> resultQueue = new LinkedBlockingQueue<AnalyzerResult>();
		final WaitableCompletionListener closeCompletionListener;

		DescriptorProvider descriptorProvider = _configuration
				.getDescriptorProvider();
		if (descriptorProvider == null) {
			descriptorProvider = new JobListDescriptorProvider(jobs);
		}
		CollectionProvider collectionProvider = _configuration
				.getCollectionProvider();
		if (collectionProvider == null) {
			collectionProvider = new BerkeleyDbCollectionProvider();
		}
		TaskRunner taskRunner = _configuration.getTaskRunner();
		if (taskRunner == null) {
			taskRunner = new SingleThreadedTaskRunner();
		}
		List<SimpleAnalyzerJob> explorerJobs = new LinkedList<SimpleAnalyzerJob>();
		List<SimpleAnalyzerJob> rowProcessingJobs = new LinkedList<SimpleAnalyzerJob>();

		categorizeJobs(jobs, descriptorProvider, explorerJobs,
				rowProcessingJobs);

		// Instantiate beans and set specific lifecycle-callbacks
		RunExplorerCallback runExplorerCallback = new RunExplorerCallback(
				dataContextProvider);
		List<AnalyzerBeanInstance> analyzerBeanInstances = new LinkedList<AnalyzerBeanInstance>();
		for (SimpleAnalyzerJob job : explorerJobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptorProvider
					.getDescriptorForClass(analyzerClass);
			AnalyzerBeanInstance analyzer = instantiateAnalyzerBean(descriptor);
			analyzer.getRunCallbacks().add(runExplorerCallback);
			analyzer.getAssignConfiguredCallbacks().add(
					new AssignConfiguredCallback(job, dataContextProvider
							.getSchemaNavigator()));
			analyzerBeanInstances.add(analyzer);
		}
		Map<Table, AnalysisRowProcessor> rowProcessors = new HashMap<Table, AnalysisRowProcessor>();
		for (SimpleAnalyzerJob job : rowProcessingJobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptorProvider
					.getDescriptorForClass(analyzerClass);
			initRowProcessingBeans(job, descriptor, analyzerBeanInstances,
					rowProcessors, dataContextProvider);
		}
		_rowProcessorAnalyzersCount = rowProcessors.size();

		// Add shared callbacks
		InitializeCallback initializeCallback = new InitializeCallback();
		ReturnResultsCallback returnResultsCallback = new ReturnResultsCallback(
				resultQueue);
		CloseCallback closeCallback = new CloseCallback();

		Collection<Task> initializeAnalyzersTasks = new LinkedList<Task>();
		Collection<Task> runAnalyzersTasks = new LinkedList<Task>();
		Collection<Task> closeAnalyzersTasks = new LinkedList<Task>();

		// create the tasks for cleaning up after running the analyzers
		closeCompletionListener = new WaitableCompletionListener(
				analyzerBeanInstances.size());
		for (AnalyzerBeanInstance analyzerBeanInstance : analyzerBeanInstances) {
			CollectResultsAndCloseAnalyzerBeanTask closeTask = new CollectResultsAndCloseAnalyzerBeanTask(
					closeCompletionListener, analyzerBeanInstance);
			closeAnalyzersTasks.add(closeTask);
		}

		// create the tasks for running the analyzers
		int numRunTasks = rowProcessors.size() + analyzerBeanInstances.size();
		CompletionListener runCompletionListener = new ScheduleTasksCompletionListener(
				taskRunner, numRunTasks, closeAnalyzersTasks);
		for (AnalyzerBeanInstance analyzerBeanInstance : analyzerBeanInstances) {
			Task runTask = analyzerBeanInstance
					.createTask(runCompletionListener);
			runAnalyzersTasks.add(runTask);
		}
		for (AnalysisRowProcessor analysisRowProcessor : rowProcessors.values()) {
			Task runTask = analysisRowProcessor
					.createTask(runCompletionListener);
			runAnalyzersTasks.add(runTask);
		}

		// create the tasks for initializing the analyzers
		CompletionListener initializeCompletionListener = new ScheduleTasksCompletionListener(
				taskRunner, analyzerBeanInstances.size(), runAnalyzersTasks);

		for (AnalyzerBeanInstance analyzerBeanInstance : analyzerBeanInstances) {
			Task initializeTask = new AssignAndInitializeTask(
					initializeCompletionListener, analyzerBeanInstance,
					collectionProvider, dataContextProvider,
					initializeCallback, returnResultsCallback, closeCallback);
			initializeAnalyzersTasks.add(initializeTask);
		}

		// begin!
		new ScheduleTasksCompletionListener(taskRunner, 1,
				initializeAnalyzersTasks).onComplete();

		logger.info("run(...) returning.");
		return new AnalysisResultFutureImpl(resultQueue,
				closeCompletionListener);
	}

	public Integer getRowProcessorCount() {
		return _rowProcessorAnalyzersCount;
	}

	private void initRowProcessingBeans(SimpleAnalyzerJob job,
			AnalyzerBeanDescriptor descriptor,
			List<AnalyzerBeanInstance> analyzerBeanInstances,
			Map<Table, AnalysisRowProcessor> rowProcessors,
			DataContextProvider dataContextProvider) {
		try {

			Map<String, String[]> columnProperties = job.getColumnProperties();
			Set<Entry<String, String[]>> columnPropertyEntries = columnProperties
					.entrySet();
			for (Entry<String, String[]> entry : columnPropertyEntries) {

				String configuredName = entry.getKey();
				ConfiguredDescriptor configuredDescriptor = descriptor
						.getConfiguredDescriptor(configuredName);
				if (configuredDescriptor == null) {
					throw new IllegalStateException(
							"Analyzer class '"
									+ descriptor.getAnalyzerClass()
									+ "' does not specify @Configured field or method with name: "
									+ configuredName);
				}

				String[] columnNames = entry.getValue();
				Column[] columns = dataContextProvider.getSchemaNavigator()
						.convertToColumns(columnNames);
				Table[] tables = MetaModelHelper.getTables(columns);

				for (Table table : tables) {
					AnalysisRowProcessor rowProcessor = rowProcessors
							.get(table);
					if (rowProcessor == null) {
						rowProcessor = new AnalysisRowProcessor(
								dataContextProvider);
						rowProcessors.put(table, rowProcessor);
					}

					Column[] columnsForAnalyzer = MetaModelHelper
							.getTableColumns(table, columns);
					rowProcessor.addColumns(columnsForAnalyzer);

					AnalyzerBeanInstance analyzerBeanInstance = instantiateAnalyzerBean(descriptor);
					analyzerBeanInstances.add(analyzerBeanInstance);

					// Add a callback for assigning @Configured properties
					AssignConfiguredRowProcessingCallback assignConfiguredCallback = new AssignConfiguredRowProcessingCallback(
							job, dataContextProvider.getSchemaNavigator(),
							columnsForAnalyzer);
					analyzerBeanInstance.getAssignConfiguredCallbacks().add(
							assignConfiguredCallback);

					// Add a callback for executing the run(...) method
					analyzerBeanInstance.getRunCallbacks().add(
							new RunRowProcessorsCallback(rowProcessor));
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not initialize analyzer based on job: " + job, e);
		}
	}

	private AnalyzerBeanInstance instantiateAnalyzerBean(
			AnalyzerBeanDescriptor descriptor) {
		try {
			Object analyzerBean = descriptor.getAnalyzerClass().newInstance();
			return new AnalyzerBeanInstance(analyzerBean, descriptor);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not instantiate analyzer bean type: "
							+ descriptor.getAnalyzerClass(), e);
		}
	}

	/**
	 * Categorize jobs corresponding to their execution type
	 * 
	 * TODO: Perhaps we can optimize a little bit if there are any beans that
	 * implement BOTH ExploringAnalyzer and RowProcessingAnalyzer. In such cases
	 * the RowProcessing execution should be used if more analyzers require the
	 * same data and the Exploring execution if not.
	 */
	private void categorizeJobs(Collection<? extends SimpleAnalyzerJob> jobs,
			DescriptorProvider descriptorProvider,
			List<SimpleAnalyzerJob> explorerJobs, List<SimpleAnalyzerJob> rowProcessingJobs)
			throws IllegalStateException {
		for (SimpleAnalyzerJob job : jobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptorProvider
					.getDescriptorForClass(analyzerClass);
			if (descriptor == null) {
				throw new IllegalStateException("No descriptor found for "
						+ analyzerClass);
			}
			if (descriptor.isExploringAnalyzer()) {
				explorerJobs.add(job);
			} else if (descriptor.isRowProcessingAnalyzer()) {
				rowProcessingJobs.add(job);
			}
		}
	}
}
