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
import java.util.Map;

import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.AnalyzerJobHelper;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.util.CollectionUtils2;

/**
 * Helper class to perform the reduction phase of {@link AnalyzerResult}s
 * collected in a {@link DistributedAnalysisResultFuture}.
 */
final class DistributedAnalysisResultReducer {

    private final AnalysisJob _masterJob;
    private final LifeCycleHelper _lifeCycleHelper;

    public DistributedAnalysisResultReducer(AnalysisJob masterJob, LifeCycleHelper lifeCycleHelper) {
        _masterJob = masterJob;
        _lifeCycleHelper = lifeCycleHelper;
    }

    public void reduce(final List<AnalysisResultFuture> results, final Map<ComponentJob, AnalyzerResult> resultMap) {
        Collection<AnalyzerJob> analyzerJobs = _masterJob.getAnalyzerJobs();
        for (AnalyzerJob masterAnalyzerJob : analyzerJobs) {
            final Collection<AnalyzerResult> slaveResults = new ArrayList<AnalyzerResult>();
            for (AnalysisResultFuture result : results) {
                if (result.isErrornous()) {
                    // error occurred!
                    return;
                }
                final Map<ComponentJob, AnalyzerResult> slaveResultMap = result.getResultMap();
                final List<AnalyzerJob> slaveAnalyzerJobs = CollectionUtils2.filterOnClass(slaveResultMap
                        .keySet(), AnalyzerJob.class);
                final AnalyzerJobHelper analyzerJobHelper = new AnalyzerJobHelper(slaveAnalyzerJobs);
                final AnalyzerJob slaveAnalyzerJob = analyzerJobHelper.getAnalyzerJob(masterAnalyzerJob);
                if (slaveAnalyzerJob == null) {
                    throw new IllegalStateException("Could not resolve slave component matching [" + masterAnalyzerJob
                            + "] in slave result: " + result);
                }

                final AnalyzerResult analyzerResult = result.getResult(slaveAnalyzerJob);
                slaveResults.add(analyzerResult);
            }
            final AnalyzerResult reducedResult = reduce(masterAnalyzerJob, slaveResults);
            resultMap.put(masterAnalyzerJob, reducedResult);
        }
    }

    private AnalyzerResult reduce(AnalyzerJob analyzerJob, Collection<AnalyzerResult> slaveResults) {
        final Class<? extends AnalyzerResultReducer<?>> reducerClass = analyzerJob.getDescriptor()
                .getResultReducerClass();

        final ComponentDescriptor<? extends AnalyzerResultReducer<?>> reducerDescriptor = Descriptors
                .ofComponent(reducerClass);

        @SuppressWarnings("unchecked")
        final AnalyzerResultReducer<AnalyzerResult> reducer = (AnalyzerResultReducer<AnalyzerResult>) reducerDescriptor
                .newInstance();

        _lifeCycleHelper.assignProvidedProperties(reducerDescriptor, reducer);
        _lifeCycleHelper.initialize(reducerDescriptor, reducer);

        final AnalyzerResult reducedResult = reducer.reduce(slaveResults);

        _lifeCycleHelper.close(reducerDescriptor, reducer);

        return reducedResult;
    }
}
