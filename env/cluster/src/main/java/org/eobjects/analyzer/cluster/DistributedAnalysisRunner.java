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
package org.eobjects.analyzer.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter.Category;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.runner.AnalysisJobMetrics;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.CompositeAnalysisListener;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.analyzer.job.runner.RowProcessingPublisher;
import org.eobjects.analyzer.job.runner.RowProcessingPublishers;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.SharedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AnalysisRunner} which executes {@link AnalysisJob}s accross a
 * distributed set of slave nodes.
 */
public final class DistributedAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(DistributedAnalysisRunner.class);

    private final ClusterManager _clusterManager;
    private final AnalyzerBeansConfiguration _configuration;
    private final CompositeAnalysisListener _analysisListener;

    public DistributedAnalysisRunner(AnalyzerBeansConfiguration configuration, ClusterManager clusterManager) {
        this(configuration, clusterManager, new AnalysisListener[0]);
    }

    public DistributedAnalysisRunner(AnalyzerBeansConfiguration configuration, ClusterManager clusterManager,
            AnalysisListener... listeners) {
        _configuration = configuration;
        _clusterManager = clusterManager;
        _analysisListener = new CompositeAnalysisListener(listeners);
    }

    /**
     * Determines if an {@link AnalysisJob} is distributable or not. If this
     * method returns false, calling {@link #run(AnalysisJob)} with the job will
     * typically throw a {@link UnsupportedOperationException}.
     * 
     * @param job
     * @return
     */
    public boolean isDistributable(final AnalysisJob job) {
        try {
            failIfJobIsUnsupported(job);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     *             if the job is not distributable (either because components
     *             are not distributable in their nature, or because some
     *             features are limited).
     */
    @Override
    public AnalysisResultFuture run(final AnalysisJob job) throws UnsupportedOperationException {
        failIfJobIsUnsupported(job);

        final JobDivisionManager jobDivisionManager = _clusterManager.getJobDivisionManager();

        final InjectionManager injectionManager = _configuration.getInjectionManager(job);
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, true);
        final RowProcessingPublishers publishers = getRowProcessingPublishers(job, lifeCycleHelper);
        final RowProcessingPublisher publisher = getRowProcessingPublisher(publishers);
        publisher.initializeConsumers(new TaskListener() {
            @Override
            public void onError(Task task, Throwable throwable) {
                logger.error("Failed to initialize consumers at master node!", throwable);
            }

            @Override
            public void onComplete(Task task) {
            }

            @Override
            public void onBegin(Task task) {
            }
        });

        // since we always use a SingleThreadedTaskRunner, the above operation
        // will be synchronized/blocking.

        final AnalysisJobMetrics analysisJobMetrics = publishers.getAnalysisJobMetrics();

        final RowProcessingMetrics rowProcessingMetrics = publisher.getRowProcessingMetrics();
        final DistributedAnalysisResultFuture resultFuture;

        _analysisListener.jobBegin(job, analysisJobMetrics);
        try {
            final int expectedRows = rowProcessingMetrics.getExpectedRows();
            final int chunks = jobDivisionManager.calculateDivisionCount(job, expectedRows);
            final int rowsPerChunk = (expectedRows +1) / chunks;

            _analysisListener.rowProcessingBegin(job, rowProcessingMetrics);

            final List<AnalysisResultFuture> results = dispatchJobs(job, chunks, rowsPerChunk);

            final DistributedAnalysisResultReducer reducer = new DistributedAnalysisResultReducer(job, lifeCycleHelper,
                    publisher);

            resultFuture = new DistributedAnalysisResultFuture(results, reducer);
        } catch (RuntimeException e) {
            _analysisListener.errorUknown(job, e);
            throw e;
        }

        if (!_analysisListener.isEmpty()) {
            awaitAndInformListener(job, analysisJobMetrics, rowProcessingMetrics, resultFuture);
        }

        return resultFuture;
    }

    /**
     * Spawns a new thread for awaiting the result future and informs the
     * {@link AnalysisListener} of the outcome.
     * 
     * @param job
     * @param analysisJobMetrics
     * @param resultFuture
     */
    private void awaitAndInformListener(final AnalysisJob job, final AnalysisJobMetrics analysisJobMetrics,
            final RowProcessingMetrics rowProcessingMetrics, final AnalysisResultFuture resultFuture) {
        SharedExecutorService.get().execute(new Runnable() {
            @Override
            public void run() {
                resultFuture.await();
                if (resultFuture.isSuccessful()) {
                    _analysisListener.rowProcessingSuccess(job, rowProcessingMetrics);
                    Set<Entry<ComponentJob, AnalyzerResult>> resultEntries = resultFuture.getResultMap().entrySet();
                    for (Entry<ComponentJob, AnalyzerResult> entry : resultEntries) {
                        final ComponentJob componentJob = entry.getKey();
                        final AnalyzerResult analyzerResult = entry.getValue();
                        if (componentJob instanceof AnalyzerJob) {
                            AnalyzerJob analyzerJob = (AnalyzerJob) componentJob;
                            _analysisListener.analyzerSuccess(job, analyzerJob, analyzerResult);
                        } else if (componentJob instanceof ExplorerJob) {
                            ExplorerJob explorerJob = (ExplorerJob) componentJob;
                            _analysisListener.explorerSuccess(job, explorerJob, analyzerResult);
                        } else {
                            logger.warn("Unexpected component job with a result: " + componentJob
                                    + ". Success state will not be notified in AnalysisListener.");
                        }
                    }
                    _analysisListener.jobSuccess(job, analysisJobMetrics);
                }
            }
        });
    }

    public List<AnalysisResultFuture> dispatchJobs(final AnalysisJob job, final int chunks, final int rowsPerChunk) {
        final List<AnalysisResultFuture> results = new ArrayList<AnalysisResultFuture>();
        for (int i = 0; i < chunks; i++) {
            final int firstRow = (i * rowsPerChunk) + 1;
            final int maxRows;
            if (i == chunks - 1) {
                maxRows = rowsPerChunk + firstRow - 1;
            } else {
                maxRows = rowsPerChunk;
            }

            final AnalysisJob slaveJob = buildSlaveJob(job, firstRow, maxRows);
            final DistributedJobContextImpl context = new DistributedJobContextImpl(_configuration, job, i, chunks);

            try {
                final AnalysisResultFuture slaveResultFuture = _clusterManager.dispatchJob(slaveJob, context);
                results.add(slaveResultFuture);
            } catch (Exception e) {
                _analysisListener.errorUknown(job, e);
                // exceptions due to dispatching jobs are added as the first of
                // the job's errors, and the rest of the execution is aborted.
                AnalysisResultFuture errorResult = new FailedAnalysisResultFuture(e);
                results.add(0, errorResult);
                break;
            }
        }
        return results;
    }

    /**
     * Creates a slave job by copying the original job and adding a
     * {@link MaxRowsFilter} as a default requirement.
     * 
     * @param job
     * @param firstRow
     * @param maxRows
     * @return
     */
    private AnalysisJob buildSlaveJob(AnalysisJob job, int firstRow, int maxRows) {
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(_configuration, job);
        try {
            final FilterJobBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilder.addFilter(MaxRowsFilter.class);
            maxRowsFilter.getConfigurableBean().setFirstRow(firstRow);
            maxRowsFilter.getConfigurableBean().setMaxRows(maxRows);

            jobBuilder.setDefaultRequirement(maxRowsFilter, MaxRowsFilter.Category.VALID);

            // in assertion/test mode do an early validation
            assert jobBuilder.isConfigured(true);

            return jobBuilder.toAnalysisJob();
        } finally {
            jobBuilder.close();
        }
    }

    private RowProcessingPublishers getRowProcessingPublishers(AnalysisJob job, LifeCycleHelper lifeCycleHelper) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        final SingleThreadedTaskRunner taskRunner = new SingleThreadedTaskRunner();

        final RowProcessingPublishers publishers = new RowProcessingPublishers(job, null, taskRunner, lifeCycleHelper,
                sourceColumnFinder);

        return publishers;
    }

    private RowProcessingPublisher getRowProcessingPublisher(RowProcessingPublishers publishers) {
        final Table[] tables = publishers.getTables();

        if (tables.length != 1) {
            throw new UnsupportedOperationException("Jobs with multiple source tables are not distributable");
        }

        final Table table = tables[0];

        final RowProcessingPublisher publisher = publishers.getRowProcessingPublisher(table);
        return publisher;
    }

    private void failIfJobIsUnsupported(AnalysisJob job) throws UnsupportedOperationException {
        failIfComponentsAreUnsupported(job.getExplorerJobs());
        failIfComponentsAreUnsupported(job.getFilterJobs());
        failIfComponentsAreUnsupported(job.getTransformerJobs());
        failIfComponentsAreUnsupported(job.getAnalyzerJobs());
    }

    private void failIfComponentsAreUnsupported(Collection<? extends ComponentJob> jobs)
            throws UnsupportedOperationException {
        for (ComponentJob job : jobs) {
            final ComponentDescriptor<?> descriptor = job.getDescriptor();
            if (descriptor instanceof BeanDescriptor) {
                final BeanDescriptor<?> beanDescriptor = (BeanDescriptor<?>) descriptor;
                final boolean distributable = beanDescriptor.isDistributable();
                if (!distributable) {
                    throw new UnsupportedOperationException("Component is not distributable: " + job);
                }
            } else {
                throw new UnsupportedOperationException("Unsupported component type: " + descriptor);
            }
        }
    }

}
