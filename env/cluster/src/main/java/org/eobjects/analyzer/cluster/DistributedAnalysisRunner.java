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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter.Category;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.InitializeMethodDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisJobMetrics;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.analyzer.job.runner.RowProcessingPublishers;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AnalysisRunner} which executes {@link AnalysisJob}s accross a
 * distributed set of slave nodes.
 */
public final class DistributedAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(DistributedAnalysisRunner.class);

    private final ClusterManager _nodeManager;
    private final AnalyzerBeansConfiguration _configuration;

    public DistributedAnalysisRunner(AnalyzerBeansConfiguration configuration, ClusterManager nodeManager) {
        _configuration = configuration;
        _nodeManager = nodeManager;
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

        final JobDivisionManager jobDivisionManager = _nodeManager.getJobDivisionManager();

        final int expectedRows = getExpectedRows(job);
        final int chunks = jobDivisionManager.calculateDivisionCount(job, expectedRows);
        final int rowsPerChunk = expectedRows / chunks;

        final InjectionManager injectionManager = _configuration.getInjectionManager(job);
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, true);

        final Map<Object, ComponentDescriptor<?>> nonDistributableComponents = initializeNonDistributableComponents(
                job, lifeCycleHelper);

        final List<AnalysisResultFuture> results = dispatchJobs(job, chunks, rowsPerChunk);

        final DistributedAnalysisResultReducer reducer = new DistributedAnalysisResultReducer(job, lifeCycleHelper,
                nonDistributableComponents);

        final DistributedAnalysisResultFuture resultFuture = new DistributedAnalysisResultFuture(results, reducer);
        return resultFuture;
    }

    public List<AnalysisResultFuture> dispatchJobs(final AnalysisJob job, final int chunks, final int rowsPerChunk) {
        final List<AnalysisResultFuture> results = new ArrayList<AnalysisResultFuture>();
        for (int i = 0; i < chunks; i++) {
            final int firstRow = (i * rowsPerChunk) + 1;
            final int maxRows;
            if (i == chunks - 1) {
                maxRows = Integer.MAX_VALUE - firstRow - 1;
            } else {
                maxRows = rowsPerChunk;
            }

            final AnalysisJob slaveJob = buildSlaveJob(job, firstRow, maxRows);
            final DistributedJobContextImpl context = new DistributedJobContextImpl(_configuration, job, i, chunks);

            try {
                final AnalysisResultFuture slaveResultFuture = _nodeManager.dispatchJob(slaveJob, context);
                results.add(slaveResultFuture);
            } catch (Exception e) {
                // exceptions due to dispatching jobs are added as the first of
                // the job's errors, and the rest of the execution is aborted.
                AnalysisResultFuture errorResult = new FailedAnalysisResultFuture(e);
                results.add(0, errorResult);
                break;
            }
        }
        return results;
    }

    private Map<Object, ComponentDescriptor<?>> initializeNonDistributableComponents(AnalysisJob job,
            LifeCycleHelper lifeCycleHelper) {
        final Map<Object, ComponentDescriptor<?>> map = new IdentityHashMap<Object, ComponentDescriptor<?>>();
        collectNonDistributableComponents(job.getExplorerJobs(), lifeCycleHelper, map);
        collectNonDistributableComponents(job.getFilterJobs(), lifeCycleHelper, map);
        collectNonDistributableComponents(job.getTransformerJobs(), lifeCycleHelper, map);
        collectNonDistributableComponents(job.getAnalyzerJobs(), lifeCycleHelper, map);

        final Set<Entry<Object, ComponentDescriptor<?>>> entries = map.entrySet();
        for (Entry<Object, ComponentDescriptor<?>> entry : entries) {
            final Object component = entry.getKey();
            final ComponentDescriptor<?> descriptor = entry.getValue();

            lifeCycleHelper.initialize(descriptor, component);
        }

        return map;
    }

    private void collectNonDistributableComponents(Collection<? extends ComponentJob> jobs,
            LifeCycleHelper lifeCycleHelper, Map<Object, ComponentDescriptor<?>> map) {
        for (ComponentJob job : jobs) {
            final ComponentDescriptor<?> descriptor = job.getDescriptor();
            final Set<InitializeMethodDescriptor> initializeMethods = descriptor.getInitializeMethods();
            for (InitializeMethodDescriptor initializeMethodDescriptor : initializeMethods) {
                if (!initializeMethodDescriptor.isDistributed()) {
                    // component needs to be initialized at master node
                    logger.info(
                            "Initializer of component '{}' is not distributable, performing initialization at master node",
                            descriptor.getDisplayName());

                    final Object instance = descriptor.newInstance();
                    map.put(instance, descriptor);

                    break;
                }
            }
        }
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
        final FilterJobBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilder.addFilter(MaxRowsFilter.class);
        maxRowsFilter.getConfigurableBean().setFirstRow(firstRow);
        maxRowsFilter.getConfigurableBean().setMaxRows(maxRows);

        jobBuilder.setDefaultRequirement(maxRowsFilter, MaxRowsFilter.Category.VALID);

        // in assertion/test mode do an early validation
        assert jobBuilder.isConfigured(true);

        return jobBuilder.toAnalysisJob();
    }

    private int getExpectedRows(AnalysisJob job) throws UnsupportedOperationException {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        final RowProcessingPublishers publishers = new RowProcessingPublishers(job, null,
                new SingleThreadedTaskRunner(), null, sourceColumnFinder);
        final Table[] tables = publishers.getTables();

        if (tables.length != 1) {
            throw new UnsupportedOperationException("Jobs with multiple source tables are not distributable");
        }

        final Table table = tables[0];

        final AnalysisJobMetrics analysisJobMetrics = publishers.buildAnalysisJobMetrics();
        final RowProcessingMetrics rowProcessingMetrics = analysisJobMetrics.getRowProcessingMetrics(table);
        final int expectedRows = rowProcessingMetrics.getExpectedRows();
        return expectedRows;
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
