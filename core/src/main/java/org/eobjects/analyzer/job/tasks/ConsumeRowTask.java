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
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.OutcomeSink;
import org.eobjects.analyzer.job.runner.OutcomeSinkImpl;
import org.eobjects.analyzer.job.runner.RowProcessingConsumer;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.schema.Table;

public final class ConsumeRowTask implements Task {

	private final Iterable<RowProcessingConsumer> _consumers;
	private final Table _table;
	private final Row _row;
	private final AnalysisListener _analysisListener;
	private final AnalysisJob _job;
	private final AtomicInteger _rowCounter;
	private final Collection<? extends Outcome> _initialOutcomes;

	/**
	 * 
	 * @param consumers
	 * @param table
	 * @param row
	 * @param rowCounter
	 * @param job
	 * @param analysisListener
	 * @param initialOutcomes
	 *            the initial list of available outcomes (if non-empty, this
	 *            will contain query-optimized outcomes)
	 */
	public ConsumeRowTask(Iterable<RowProcessingConsumer> consumers, Table table, Row row, AtomicInteger rowCounter,
			AnalysisJob job, AnalysisListener analysisListener, Collection<? extends Outcome> initialOutcomes) {
		_consumers = consumers;
		_table = table;
		_row = row;
		_rowCounter = rowCounter;
		_job = job;
		_analysisListener = analysisListener;
		_initialOutcomes = initialOutcomes;
	}

	@Override
	public void execute() {
		OutcomeSink outcomeSink = new OutcomeSinkImpl(_initialOutcomes);

		final int distinctCount = 1;
		int rowNumber = _rowCounter.addAndGet(distinctCount);
		List<InputRow> inputRows = new ArrayList<InputRow>();
		inputRows.add(new MetaModelInputRow(rowNumber, _row));
		for (RowProcessingConsumer consumer : _consumers) {
			if (consumer.isConcurrent()) {
				handleConsumer(outcomeSink, distinctCount, inputRows, consumer);
			} else {
				synchronized (consumer) {
					handleConsumer(outcomeSink, distinctCount, inputRows, consumer);
				}
			}
		}
		_analysisListener.rowProcessingProgress(_job, _table, rowNumber);
	}

	private void handleConsumer(OutcomeSink outcomeSink, final int distinctCount, List<InputRow> rows,
			RowProcessingConsumer consumer) {
		final List<InputRow> newRows = new ArrayList<InputRow>();

		// used for optimization: if output rows aren't modified by consumers
		// (they return null), then we don't need to rebuild the rows list.
		boolean outputRowsSame = true;

		for (InputRow row : rows) {
			boolean process = consumer.satisfiedForConsume(outcomeSink.getOutcomes(), row.getInputColumns());
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
