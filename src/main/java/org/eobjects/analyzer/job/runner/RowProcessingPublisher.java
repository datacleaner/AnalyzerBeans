package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.job.concurrent.NestedCompletionListener;
import org.eobjects.analyzer.job.concurrent.ScheduleTasksCompletionListener;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.AssignCallbacksAndInitializeTask;
import org.eobjects.analyzer.job.tasks.CloseBeanTask;
import org.eobjects.analyzer.job.tasks.CollectResultsAndCloseAnalyzerBeanTask;
import org.eobjects.analyzer.job.tasks.RunRowProcessingPublisherTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AnalyzerLifeCycleCallback;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.LifeCycleCallback;
import org.eobjects.analyzer.lifecycle.ReturnResultsCallback;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public final class RowProcessingPublisher {

	private final Set<Column> _physicalColumns = new HashSet<Column>();
	private final List<RowProcessingConsumer> _consumers = new ArrayList<RowProcessingConsumer>();
	private final DataContextProvider _dataContextProvider;
	private final CollectionProvider _collectionProvider;
	private final Table _table;

	public RowProcessingPublisher(DataContextProvider dataContextProvider, CollectionProvider collectionProvider, Table table) {
		if (dataContextProvider == null) {
			throw new IllegalArgumentException("DataContextProvider cannot be null");
		}
		if (collectionProvider == null) {
			throw new IllegalArgumentException("CollectionProvider cannot be null");
		}
		if (table == null) {
			throw new IllegalArgumentException("Table cannot be null");
		}

		_dataContextProvider = dataContextProvider;
		_collectionProvider = collectionProvider;
		_table = table;
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
		Column[] columnArray = _physicalColumns.toArray(new Column[_physicalColumns.size()]);

		DataContext dataContext = _dataContextProvider.getDataContext();
		Query q = dataContext.query().from(_table).select(columnArray).toQuery();
		SelectItem countAllItem = SelectItem.getCountAllItem();
		q.select(countAllItem);
		q.groupBy(columnArray);

		Iterable<RowProcessingConsumer> consumers = createProcessOrderedConsumerList(_consumers);

		DataSet dataSet = dataContext.executeQuery(q);
		while (dataSet.next()) {
			Row metaModelRow = dataSet.getRow();
			Number distinctCount = (Number) metaModelRow.getValue(countAllItem);
			InputRow inputRow = new MetaModelInputRow(metaModelRow);

			for (RowProcessingConsumer rowProcessingConsumer : consumers) {
				inputRow = rowProcessingConsumer.consume(inputRow, distinctCount.intValue());
			}
		}
	}

	protected static List<RowProcessingConsumer> createProcessOrderedConsumerList(
			Collection<? extends RowProcessingConsumer> consumers) {
		List<RowProcessingConsumer> result = new LinkedList<RowProcessingConsumer>();

		Collection<RowProcessingConsumer> remainingConsumers = new LinkedList<RowProcessingConsumer>(consumers);
		Set<InputColumn<?>> availableVirtualColumns = new HashSet<InputColumn<?>>();

		while (!remainingConsumers.isEmpty()) {
			boolean changed = false;
			for (Iterator<RowProcessingConsumer> it = remainingConsumers.iterator(); it.hasNext();) {
				RowProcessingConsumer consumer = it.next();

				boolean accepted = true;

				InputColumn<?>[] requiredInput = consumer.getRequiredInput();
				for (InputColumn<?> inputColumn : requiredInput) {
					if (!inputColumn.isPhysicalColumn()) {
						if (!availableVirtualColumns.contains(inputColumn)) {
							accepted = false;
							break;
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
		addConsumer(new AnalyzerConsumer(analyzerBeanInstance, analyzerJob, inputColumns));
	}

	public void addTransformerBean(TransformerBeanInstance transformerBeanInstance, TransformerJob transformerJob,
			InputColumn<?>[] inputColumns) {
		addConsumer(new TransformerConsumer(transformerBeanInstance, transformerJob, inputColumns));
	}

	private void addConsumer(RowProcessingConsumer consumer) {
		_consumers.add(consumer);
	}

	public List<Task> createInitialTasks(TaskRunner taskRunner, Queue<AnalyzerResult> resultQueue,
			CompletionListener rowProcessorPublishersDoneCompletionListener) {
		int numConsumers = _consumers.size();

		CompletionListener closeCompletionListener = new NestedCompletionListener("row processor consumers", numConsumers,
				rowProcessorPublishersDoneCompletionListener);

		List<Task> closeTasks = new ArrayList<Task>(numConsumers);
		for (RowProcessingConsumer consumer : _consumers) {
			closeTasks.add(createCloseTask(consumer, resultQueue, closeCompletionListener));
		}

		CompletionListener runCompletionListener = new ScheduleTasksCompletionListener("run row processing", taskRunner, 1,
				closeTasks);

		Collection<Task> runTasksToSchedule = new ArrayList<Task>(1);
		runTasksToSchedule.add(new RunRowProcessingPublisherTask(this, runCompletionListener));

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
		if (consumer instanceof TransformerConsumer) {
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

		if (consumer instanceof TransformerConsumer) {
			TransformerConsumer transformerConsumer = (TransformerConsumer) consumer;
			TransformerBeanInstance transformerBeanInstance = transformerConsumer.getBeanInstance();

			return new AssignCallbacksAndInitializeTask(completionListener, transformerBeanInstance, _collectionProvider,
					_dataContextProvider, assignConfiguredCallback, initializeCallback, closeCallback);
		} else if (consumer instanceof AnalyzerConsumer) {
			AnalyzerConsumer analyzerConsumer = (AnalyzerConsumer) consumer;
			AnalyzerBeanInstance analyzerBeanInstance = analyzerConsumer.getBeanInstance();
			AnalyzerLifeCycleCallback returnResultsCallback = new ReturnResultsCallback(resultQueue);

			return new AssignCallbacksAndInitializeTask(completionListener, analyzerBeanInstance, _collectionProvider,
					_dataContextProvider, assignConfiguredCallback, initializeCallback, null, returnResultsCallback,
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
