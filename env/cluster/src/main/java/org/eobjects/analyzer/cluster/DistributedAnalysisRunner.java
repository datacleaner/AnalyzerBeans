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
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter.Category;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisJobMetrics;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.analyzer.job.runner.RowProcessingPublishers;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.metamodel.schema.Table;

/**
 * An {@link AnalysisRunner} which executes {@link AnalysisJob}s accross a
 * distributed set of slave nodes.
 */
public final class DistributedAnalysisRunner implements AnalysisRunner {

    private static final int MIN_RECORDS_PER_CHUNK = 100;

    private final ClusterManager _nodeManager;
    private final AnalyzerBeansConfiguration _configuration;

    public DistributedAnalysisRunner(AnalyzerBeansConfiguration configuration, ClusterManager nodeManager) {
        _configuration = configuration;
        _nodeManager = nodeManager;
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

        final List<ClusterNode> availableNodes = _nodeManager.getAvailableNodes();

        final int expectedRows = getExpectedRows(job);
        final int chunks = calculateChunks(availableNodes, expectedRows);
        final int rowsPerChunk = expectedRows / chunks;

        final List<ClusterNode> targetNodes = pickTargetNodes(availableNodes, chunks);

        final List<AnalysisResultFuture> results = new ArrayList<AnalysisResultFuture>();
        for (int i = 0; i < chunks; i++) {
            final int firstRow = (i * rowsPerChunk) + 1;
            final int maxRows;
            if (i == chunks - 1) {
                maxRows = Integer.MAX_VALUE - rowsPerChunk - 1;
            } else {
                maxRows = rowsPerChunk;
            }

            final AnalysisJob slaveJob = buildSlaveJob(job, firstRow, maxRows);
            final ClusterNode targetNode = targetNodes.get(i);

            final AnalysisResultFuture slaveResultFuture = _nodeManager.dispatchJob(targetNode, slaveJob);
            results.add(slaveResultFuture);
        }

        final DistributedAnalysisResultFuture resultFuture = new DistributedAnalysisResultFuture(results);
        return resultFuture;
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

    private List<ClusterNode> pickTargetNodes(List<ClusterNode> availableNodes, int chunks) {
        final int nodeCount = availableNodes.size();
        if (chunks > nodeCount) {
            throw new IllegalStateException("Chunks cannot be larger than the available amount of nodes");
        } else if (chunks == nodeCount) {
            // all nodes will be targeted
            return availableNodes;
        } else {
            // select randomly
            Collections.shuffle(availableNodes);
            return availableNodes.subList(0, chunks);
        }
    }

    /**
     * Calculates the amount of chunks of data (each represented as a slave job)
     * to create.
     * 
     * @param nodes
     * @param expectedRows
     * @return
     */
    private int calculateChunks(List<ClusterNode> nodes, int expectedRows) {
        final int nodeCount = nodes.size();
        final int rowsPerChunk = expectedRows / nodeCount;
        if (rowsPerChunk >= MIN_RECORDS_PER_CHUNK) {
            // we'll utilize all nodes
            return nodeCount;
        }

        final int chunkCount = (int) Math.ceil((1.0d * expectedRows / MIN_RECORDS_PER_CHUNK));
        return chunkCount;
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
        Collection<ExplorerJob> explorerJobs = job.getExplorerJobs();
        if (explorerJobs != null && !explorerJobs.isEmpty()) {
            throw new UnsupportedOperationException("Explorer jobs are not distributable.");
        }

        // TODO : check distributability of row processing components (needs new
        // API)
    }

}
