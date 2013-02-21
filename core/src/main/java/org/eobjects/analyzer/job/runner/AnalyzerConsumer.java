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

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;

final class AnalyzerConsumer extends AbstractRowProcessingConsumer implements RowProcessingConsumer {

    private final AnalysisJob _job;
    private final AnalyzerJob _analyzerJob;
    private final Analyzer<?> _analyzer;
    private final InputColumn<?>[] _inputColumns;
    private final AnalysisListener _analysisListener;
    private final boolean _concurrent;

    public AnalyzerConsumer(Analyzer<?> analyzer, AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns,
            RowProcessingPublishers publishers) {
        super(analyzerJob, analyzerJob);
        _analyzer = analyzer;
        _analyzerJob = analyzerJob;
        _inputColumns = inputColumns;
        if (publishers == null) {
            _job = null;
            _analysisListener = null;
        } else {
            _job = publishers.getAnalysisJob();
            _analysisListener = publishers.getAnalysisListener();
        }

        Concurrent concurrent = analyzerJob.getDescriptor().getAnnotation(Concurrent.class);
        if (concurrent == null) {
            // analyzers are by default not concurrent
            _concurrent = false;
        } else {
            _concurrent = concurrent.value();
        }
    }

    @Override
    public Analyzer<?> getComponent() {
        return _analyzer;
    }

    @Override
    public boolean isConcurrent() {
        return _concurrent;
    }

    @Override
    public InputColumn<?>[] getRequiredInput() {
        return _inputColumns;
    }

    @Override
    public void consume(InputRow row, int distinctCount, OutcomeSink outcomes, RowProcessingChain chain) {
        try {
            _analyzer.run(row, distinctCount);
            chain.processNext(row, distinctCount, outcomes);
        } catch (RuntimeException e) {
            _analysisListener.errorInAnalyzer(_job, _analyzerJob, row, e);
        }
    }

    @Override
    public AnalyzerJob getComponentJob() {
        return _analyzerJob;
    }

    @Override
    public String toString() {
        return "AnalyzerConsumer[" + _analyzer + "]";
    }
}
