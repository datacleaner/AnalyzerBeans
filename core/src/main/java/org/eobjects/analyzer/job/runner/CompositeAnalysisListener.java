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
import java.util.List;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * {@link AnalysisListener} that wraps a list of inner listeners. Makes life easier for the invokers of the listeners.
 */
public final class CompositeAnalysisListener implements AnalysisListener {

    private final List<AnalysisListener> _delegates;

    public CompositeAnalysisListener(AnalysisListener[] delegates) {
        _delegates = new ArrayList<AnalysisListener>(delegates.length);
        for (AnalysisListener analysisListener : delegates) {
            addDelegate(analysisListener);
        }
    }

    public CompositeAnalysisListener(AnalysisListener firstDelegate, AnalysisListener... delegates) {
        _delegates = new ArrayList<AnalysisListener>(1 + delegates.length);
        _delegates.add(firstDelegate);
        for (AnalysisListener analysisListener : delegates) {
            addDelegate(analysisListener);
        }
    }

    public void addDelegate(AnalysisListener analysisListener) {
        _delegates.add(analysisListener);
    }

    @Override
    public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.jobBegin(job, metrics);
        }
    }

    @Override
    public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.jobSuccess(job, metrics);
        }
    }

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.rowProcessingBegin(job, metrics);
        }
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
        for (AnalysisListener delegate : _delegates) {
            delegate.rowProcessingProgress(job, metrics, currentRow);
        }
    }

    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.rowProcessingSuccess(job, metrics);
        }
    }

    @Override
    public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.analyzerBegin(job, analyzerJob, metrics);
        }
    }

    @Override
    public void explorerBegin(AnalysisJob job, ExplorerJob explorerJob, ExplorerMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.explorerBegin(job, explorerJob, metrics);
        }
    }

    @Override
    public void explorerSuccess(AnalysisJob job, ExplorerJob explorerJob, AnalyzerResult result) {
        for (AnalysisListener delegate : _delegates) {
            delegate.explorerSuccess(job, explorerJob, result);
        }
    }

    @Override
    public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
        for (AnalysisListener delegate : _delegates) {
            delegate.analyzerSuccess(job, analyzerJob, result);
        }
    }

    @Override
    public void errorInFilter(AnalysisJob job, FilterJob filterJob, InputRow row, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            delegate.errorInFilter(job, filterJob, row, throwable);
        }
    }

    @Override
    public void errorInExplorer(AnalysisJob job, ExplorerJob explorerJob, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            delegate.errorInExplorer(job, explorerJob, throwable);
        }
    }

    @Override
    public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, InputRow row, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            delegate.errorInTransformer(job, transformerJob, row, throwable);
        }
    }

    @Override
    public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, InputRow row, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            delegate.errorInAnalyzer(job, analyzerJob, row, throwable);
        }
    }

    @Override
    public void errorUknown(AnalysisJob job, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            delegate.errorUknown(job, throwable);
        }
    }
}
