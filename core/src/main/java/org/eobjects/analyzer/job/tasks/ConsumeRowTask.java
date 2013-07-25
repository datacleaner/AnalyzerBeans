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
package org.eobjects.analyzer.job.tasks;

import java.util.List;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.OutcomeSink;
import org.eobjects.analyzer.job.runner.RowProcessingChain;
import org.eobjects.analyzer.job.runner.RowProcessingConsumer;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;

/**
 * A {@link Task} that dispatches ("consumes") a record to all relevant
 * {@link RowProcessingConsumer}s (eg. analyzerbeans components).
 */
public final class ConsumeRowTask implements Task, RowProcessingChain {

    private final List<RowProcessingConsumer> _consumers;
    private final int _consumerIndex;
    private final RowProcessingMetrics _rowProcessingMetrics;
    private final InputRow _row;
    private final AnalysisListener _analysisListener;
    private final OutcomeSink _outcomes;

    /**
     * 
     * @param consumers
     * @param consumerIndex
     * @param rowProcessingMetrics
     * @param row
     * @param analysisListener
     * @param outcomes
     *            the initial list of available outcomes (if non-empty, this
     *            will contain query-optimized outcomes)
     */
    public ConsumeRowTask(final List<RowProcessingConsumer> consumers, final int consumerIndex,
            final RowProcessingMetrics rowProcessingMetrics, final InputRow row,
            final AnalysisListener analysisListener, final OutcomeSink outcomes) {
        _consumers = consumers;
        _consumerIndex = consumerIndex;
        _rowProcessingMetrics = rowProcessingMetrics;
        _row = row;
        _analysisListener = analysisListener;
        _outcomes = outcomes;
    }

    @Override
    public void execute() {
        execute(true);
    }

    private void execute(final boolean notifyListener) {
        if (_consumerIndex >= _consumers.size()) {
            // finished!
            return;
        }

        final RowProcessingConsumer consumer = _consumers.get(_consumerIndex);
        if (consumer.isConcurrent()) {
            handleConsumer(_row, consumer);
        } else {
            synchronized (consumer) {
                handleConsumer(_row, consumer);
            }
        }

        if (notifyListener) {
            _analysisListener.rowProcessingProgress(_rowProcessingMetrics.getAnalysisJobMetrics().getAnalysisJob(),
                    _rowProcessingMetrics, _row.getId());
        }
    }

    private void handleConsumer(final InputRow row, final RowProcessingConsumer consumer) {
        final int distinctCount = 1;
        final boolean process = consumer.satisfiedForConsume(_outcomes.getOutcomes(), row);
        if (process) {
            final RowProcessingChain chain = this;
            consumer.consume(row, distinctCount, _outcomes, chain);
        } else {
            // jump to the next step
            processNext(row, distinctCount, _outcomes);
        }
    }

    @Override
    public void processNext(InputRow row, int distinctCount, OutcomeSink outcomes) {
        final int nextIndex = _consumerIndex + 1;
        final ConsumeRowTask subtask = new ConsumeRowTask(_consumers, nextIndex, _rowProcessingMetrics, row,
                _analysisListener, outcomes);
        subtask.execute(false);
    }
}
