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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
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
import org.eobjects.analyzer.job.tasks.CloseBeanTaskListener;
import org.eobjects.analyzer.job.tasks.CollectResultsTask;
import org.eobjects.analyzer.job.tasks.ConsumeRowTask;
import org.eobjects.analyzer.job.tasks.InitializeReferenceDataTask;
import org.eobjects.analyzer.job.tasks.InitializeTask;
import org.eobjects.analyzer.job.tasks.RunRowProcessingPublisherTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RowProcessingPublisher {

    private final static Logger logger = LoggerFactory.getLogger(RowProcessingPublisher.class);

    private final AnalysisJob _analysisJob;
    private final Table _table;
    private final Set<Column> _physicalColumns = new HashSet<Column>();
    private final List<RowProcessingConsumer> _consumers = new ArrayList<RowProcessingConsumer>();
    private final TaskRunner _taskRunner;
    private final AnalysisListener _analysisListener;
    private final LifeCycleHelper _lifeCycleHelper;
    private final LazyRef<RowProcessingQueryOptimizer> _queryOptimizerRef;

    public RowProcessingPublisher(AnalysisJob analysisJob, Table table, TaskRunner taskRunner,
            AnalysisListener analysisListener, LifeCycleHelper lifeCycleHelper) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        if (taskRunner == null) {
            throw new IllegalArgumentException("TaskRunner cannot be null");
        }
        _analysisJob = analysisJob;
        _table = table;
        _taskRunner = taskRunner;
        _analysisListener = analysisListener;
        _lifeCycleHelper = lifeCycleHelper;

        _queryOptimizerRef = createQueryOptimizerRef();
    }

    /**
     * Inspects the row processed tables primary keys. If all primary keys are
     * in the source columns of the AnalysisJob, they will be added to the
     * physically queried columns.
     * 
     * Adding the primary keys to the query is a trade-off: It helps a lot in
     * making eg. annotated rows referenceable to the source table, but it may
     * also potentially make the job heavier to execute since a lot of (unique)
     * values will be retrieved.
     */
    public void addPrimaryKeysIfSourced() {
        Column[] primaryKeyColumns = _table.getPrimaryKeys();
        if (primaryKeyColumns == null || primaryKeyColumns.length == 0) {
            logger.info("No primary keys defined for table {}, not pre-selecting primary keys", _table.getName());
            return;
        }

        final Collection<InputColumn<?>> sourceInputColumns = _analysisJob.getSourceColumns();
        final List<Column> sourceColumns = CollectionUtils.map(sourceInputColumns, new Func<InputColumn<?>, Column>() {
            @Override
            public Column eval(InputColumn<?> inputColumn) {
                return inputColumn.getPhysicalColumn();
            }
        });

        for (Column primaryKeyColumn : primaryKeyColumns) {
            if (!sourceColumns.contains(primaryKeyColumn)) {
                logger.info("Primary key column {} not added to source columns, not pre-selecting primary keys");
                return;
            }
        }

        addPhysicalColumns(primaryKeyColumns);
    }

    private LazyRef<RowProcessingQueryOptimizer> createQueryOptimizerRef() {
        return new LazyRef<RowProcessingQueryOptimizer>() {
            @Override
            protected RowProcessingQueryOptimizer fetch() {
                final Datastore datastore = _analysisJob.getDatastore();
                final DatastoreConnection con = datastore.openConnection();
                try {
                    final DataContext dataContext = con.getDataContext();

                    final Column[] columnArray = _physicalColumns.toArray(new Column[_physicalColumns.size()]);
                    final Query baseQuery = dataContext.query().from(_table).select(columnArray).toQuery();

                    logger.debug("Base query for row processing: {}", baseQuery);

                    final List<RowProcessingConsumer> sortedConsumers = sortConsumers(_consumers);

                    final RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore,
                            sortedConsumers, baseQuery);
                    return optimizer;
                } finally {
                    con.close();
                }
            }
        };
    }

    /**
     * Sorts a list of consumers into their execution order
     * 
     * @param consumers
     * @return
     */
    public static List<RowProcessingConsumer> sortConsumers(List<RowProcessingConsumer> consumers) {
        final RowProcessingConsumerSorter sorter = new RowProcessingConsumerSorter(consumers);
        final List<RowProcessingConsumer> sortedConsumers = sorter.createProcessOrderedConsumerList();
        if (logger.isDebugEnabled()) {
            logger.debug("Row processing order ({} consumers):", sortedConsumers.size());
            int i = 1;
            for (RowProcessingConsumer rowProcessingConsumer : sortedConsumers) {
                logger.debug(" {}) {}", i, rowProcessingConsumer);
                i++;
            }
        }
        return sortedConsumers;
    }

    public void initialize() {
        // can safely load query optimizer in separate thread here
        _queryOptimizerRef.requestLoad();
    }

    public void addPhysicalColumns(Column... columns) {
        for (Column column : columns) {
            if (!_table.equals(column.getTable())) {
                throw new IllegalArgumentException("Column does not pertain to the correct table. Expected table: "
                        + _table + ", actual table: " + column.getTable());
            }
            _physicalColumns.add(column);
        }
    }

    private RowProcessingQueryOptimizer getQueryOptimizer() {
        return _queryOptimizerRef.get();
    }

    public Query getQuery() {
        return getQueryOptimizer().getOptimizedQuery();
    }

    public void run(RowProcessingMetrics rowProcessingMetrics) {
        final RowProcessingQueryOptimizer queryOptimizer = getQueryOptimizer();
        final Query finalQuery = queryOptimizer.getOptimizedQuery();

        final RowIdGenerator idGenerator;
        if (finalQuery.getFirstRow() == null) {
            idGenerator = new SimpleRowIdGenerator();
        } else {
            idGenerator = new SimpleRowIdGenerator(finalQuery.getFirstRow());
        }

        for (RowProcessingConsumer rowProcessingConsumer : _consumers) {
            if (rowProcessingConsumer instanceof AnalyzerConsumer) {
                final AnalyzerConsumer analyzerConsumer = (AnalyzerConsumer) rowProcessingConsumer;
                final AnalyzerJob analyzerJob = analyzerConsumer.getComponentJob();
                final AnalyzerMetrics metrics = rowProcessingMetrics.getAnalysisJobMetrics().getAnalyzerMetrics(
                        analyzerJob);
                _analysisListener.analyzerBegin(_analysisJob, analyzerJob, metrics);
            }

            if (rowProcessingConsumer instanceof TransformerConsumer) {
                ((TransformerConsumer) rowProcessingConsumer).setRowIdGenerator(idGenerator);
            }
        }
        final List<RowProcessingConsumer> consumers = queryOptimizer.getOptimizedConsumers();
        final Collection<? extends Outcome> availableOutcomes = queryOptimizer.getOptimizedAvailableOutcomes();

        _analysisListener.rowProcessingBegin(_analysisJob, rowProcessingMetrics);

        // TODO: Needs to delegate errors downstream
        final RowConsumerTaskListener taskListener = new RowConsumerTaskListener(_analysisJob, _analysisListener,
                _taskRunner);

        final Datastore datastore = _analysisJob.getDatastore();
        final DatastoreConnection con = datastore.openConnection();

        try {
            final DataContext dataContext = con.getDataContext();
            final DataSet dataSet = dataContext.executeQuery(finalQuery);

            // represents the distinct count of rows as well as the number of
            // tasks
            // to execute
            int numTasks = 0;
            
            try {
                while (dataSet.next()) {
                    if (taskListener.isErrornous()) {
                        break;
                    }
                    
                    final Row metaModelRow = dataSet.getRow();
                    final int rowId = idGenerator.nextPhysicalRowId();
                    final MetaModelInputRow inputRow = new MetaModelInputRow(rowId, metaModelRow);
                    final OutcomeSink outcomes = new OutcomeSinkImpl(availableOutcomes);
                    
                    final ConsumeRowTask task = new ConsumeRowTask(consumers, 0, rowProcessingMetrics, inputRow,
                            _analysisListener, outcomes);
                    _taskRunner.run(task, taskListener);
                    
                    numTasks++;
                }
            } finally {
                dataSet.close();
            }
            taskListener.awaitTasks(numTasks);

        } finally {
            con.close();
        }

        if (!taskListener.isErrornous()) {
            _analysisListener.rowProcessingSuccess(_analysisJob, rowProcessingMetrics);
        }
    }

    public void addAnalyzerBean(Analyzer<?> analyzer, AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns) {
        addConsumer(new AnalyzerConsumer(_analysisJob, analyzer, analyzerJob, inputColumns, _analysisListener));
    }

    public void addTransformerBean(Transformer<?> transformer, TransformerJob transformerJob,
            InputColumn<?>[] inputColumns) {
        addConsumer(new TransformerConsumer(_analysisJob, transformer, transformerJob, inputColumns, _analysisListener));
    }

    public void addFilterBean(Filter<?> filter, FilterJob filterJob, InputColumn<?>[] inputColumns) {
        addConsumer(new FilterConsumer(_analysisJob, filter, filterJob, inputColumns, _analysisListener));
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
    
    public List<RowProcessingConsumer> getConfigurableConsumers() {
        final List<RowProcessingConsumer> configurableConsumers = CollectionUtils.filter(_consumers,
                new Predicate<RowProcessingConsumer>() {
                    @Override
                    public Boolean eval(RowProcessingConsumer input) {
                        return input.getComponentJob() instanceof ConfigurableBeanJob<?>;
                    }
                });
        return configurableConsumers;
    }

    public List<TaskRunnable> createInitialTasks(TaskRunner taskRunner, Queue<JobAndResult> resultQueue,
            TaskListener rowProcessorPublishersTaskListener, Datastore datastore, AnalysisJobMetrics analysisJobMetrics) {

        final List<RowProcessingConsumer> configurableConsumers = getConfigurableConsumers();
        int numConfigurableConsumers = configurableConsumers.size();

        final TaskListener closeTaskListener = new JoinTaskListener(numConfigurableConsumers,
                rowProcessorPublishersTaskListener);

        final List<TaskRunnable> closeTasks = new ArrayList<TaskRunnable>(numConfigurableConsumers);
        for (RowProcessingConsumer consumer : configurableConsumers) {
            Task closeTask = createCloseTask(consumer, resultQueue);
            if (closeTask == null) {
                closeTasks.add(new TaskRunnable(null, closeTaskListener));
            } else {
                closeTasks.add(new TaskRunnable(closeTask, closeTaskListener));
            }
            closeTasks.add(new TaskRunnable(null, new CloseBeanTaskListener(_lifeCycleHelper, consumer
                    .getComponentJob().getDescriptor(), consumer.getComponent())));
        }

        final TaskListener runCompletionListener = new ForkTaskListener("run row processing", taskRunner, closeTasks);

        final RowProcessingMetrics rowProcessingMetrics = analysisJobMetrics.getRowProcessingMetrics(_table);
        final RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this, rowProcessingMetrics);

        final TaskListener referenceDataInitFinishedListener = new ForkTaskListener("Initialize row consumers",
                taskRunner, Arrays.asList(new TaskRunnable(runTask, runCompletionListener)));

        final RunNextTaskTaskListener joinFinishedListener = new RunNextTaskTaskListener(_taskRunner,
                new InitializeReferenceDataTask(_lifeCycleHelper), referenceDataInitFinishedListener);
        final TaskListener initFinishedListener = new JoinTaskListener(numConfigurableConsumers, joinFinishedListener);

        final List<TaskRunnable> initTasks = new ArrayList<TaskRunnable>(numConfigurableConsumers);
        for (RowProcessingConsumer consumer : configurableConsumers) {
            initTasks.add(createInitTask(consumer, initFinishedListener, resultQueue));
        }
        return initTasks;
    }

    private Task createCloseTask(RowProcessingConsumer consumer, Queue<JobAndResult> resultQueue) {
        if (consumer instanceof TransformerConsumer || consumer instanceof FilterConsumer) {
            return null;
        } else if (consumer instanceof AnalyzerConsumer) {
            AnalyzerConsumer analyzerConsumer = (AnalyzerConsumer) consumer;
            Analyzer<?> analyzer = analyzerConsumer.getComponent();
            return new CollectResultsTask(analyzer, _analysisJob, consumer.getComponentJob(), resultQueue,
                    _analysisListener);
        } else {
            throw new IllegalStateException("Unknown consumer type: " + consumer);
        }
    }

    private TaskRunnable createInitTask(RowProcessingConsumer consumer, TaskListener listener,
            Queue<JobAndResult> resultQueue) {
        ComponentJob componentJob = consumer.getComponentJob();
        Object component = consumer.getComponent();
        BeanConfiguration configuration = ((ConfigurableBeanJob<?>) componentJob).getConfiguration();
        ComponentDescriptor<?> descriptor = componentJob.getDescriptor();

        InitializeTask task = new InitializeTask(_lifeCycleHelper, descriptor, component, configuration);
        return new TaskRunnable(task, listener);
    }

    @Override
    public String toString() {
        return "RowProcessingPublisher[table=" + _table.getQualifiedLabel() + ", consumers=" + _consumers.size() + "]";
    }

    public AnalyzerJob[] getAnalyzerJobs() {
        List<AnalyzerJob> analyzerJobs = new ArrayList<AnalyzerJob>();
        for (RowProcessingConsumer consumer : _consumers) {
            if (consumer instanceof AnalyzerConsumer) {
                AnalyzerJob analyzerJob = ((AnalyzerConsumer) consumer).getComponentJob();
                analyzerJobs.add(analyzerJob);
            }
        }
        return analyzerJobs.toArray(new AnalyzerJob[analyzerJobs.size()]);
    }
}
