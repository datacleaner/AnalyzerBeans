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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.OutcomeSink;
import org.eobjects.analyzer.job.runner.OutcomeSinkImpl;
import org.eobjects.analyzer.job.runner.RowProcessingConsumer;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.metamodel.data.Row;

/**
 * A {@link Task} that dispatches ("consumes") a record to all relevant
 * {@link RowProcessingConsumer}s (eg. analyzerbeans components).
 * 
 * @author Kasper Sørensen
 */
public final class ConsumeRowTask implements Task {

	private final Iterable<RowProcessingConsumer> _consumers;
	private final RowProcessingMetrics _rowProcessingMetrics;
	private final int _rowNumber;
	private final Row _row;
	private final AnalysisListener _analysisListener;
	private final Collection<? extends Outcome> _initialOutcomes;

	/**
	 * 
	 * @param consumers
	 * @param rowProcessingMetrics
	 * @param row
	 * @param rowCounter
	 * @param analysisListener
	 * @param initialOutcomes
	 *            the initial list of available outcomes (if non-empty, this
	 *            will contain query-optimized outcomes)
	 */
	public ConsumeRowTask(Iterable<RowProcessingConsumer> consumers, RowProcessingMetrics rowProcessingMetrics, Row row,
			int rowNumber, AnalysisListener analysisListener, Collection<? extends Outcome> initialOutcomes) {
		_consumers = consumers;
		_rowProcessingMetrics = rowProcessingMetrics;
		_row = row;
		_rowNumber = rowNumber;
		_analysisListener = analysisListener;
		_initialOutcomes = initialOutcomes;
	}

	@Override
	public void execute() {
		OutcomeSink outcomeSink = new OutcomeSinkImpl(_initialOutcomes);

		final int distinctCount = 1;
		List<InputRow> inputRows = new ArrayList<InputRow>();
		inputRows.add(new MetaModelInputRow(_rowNumber, _row));
		for (RowProcessingConsumer consumer : _consumers) {
			if (consumer.isConcurrent()) {
				handleConsumer(outcomeSink, distinctCount, inputRows, consumer);
			} else {
				synchronized (consumer) {
					handleConsumer(outcomeSink, distinctCount, inputRows, consumer);
				}
			}
		}
		_analysisListener.rowProcessingProgress(_rowProcessingMetrics.getAnalysisJobMetrics().getAnalysisJob(),
				_rowProcessingMetrics, _rowNumber);
	}

	private void handleConsumer(OutcomeSink outcomeSink, final int distinctCount, List<InputRow> rows,
			RowProcessingConsumer consumer) {
		final List<InputRow> newRows = new ArrayList<InputRow>();

		// used for optimization: if output rows aren't modified by consumers
		// (they return null), then we don't need to rebuild the rows list.
		boolean outputRowsSame = true;

		for (InputRow row : rows) {
			boolean process = consumer.satisfiedForConsume(outcomeSink.getOutcomes(), row);
			if (process) {
				InputRow[] outputRows = consumer.consume(row, distinctCount, outcomeSink);
				if (outputRows != null) {
					outputRowsSame = false;
					for (InputRow newRow : outputRows) {
						newRows.add(newRow);
					}
				}
			}
		}

		if (!outputRowsSame) {
			rows.clear();
			rows.addAll(newRows);
		}
	}
}
