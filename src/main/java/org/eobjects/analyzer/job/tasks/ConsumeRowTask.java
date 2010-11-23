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

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.OutcomeSink;
import org.eobjects.analyzer.job.runner.OutcomeSinkImpl;
import org.eobjects.analyzer.job.runner.RowProcessingConsumer;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Table;

public final class ConsumeRowTask implements Task {

	private final Iterable<RowProcessingConsumer> _consumers;
	private final Table _table;
	private final Row _row;
	private final AnalysisListener _analysisListener;
	private final AnalysisJob _job;
	private final SelectItem _countAllItem;
	private final AtomicInteger _rowCounter;

	public ConsumeRowTask(Iterable<RowProcessingConsumer> consumers, Table table, Row row, SelectItem countAllItem,
			AtomicInteger rowCounter, AnalysisJob job, AnalysisListener analysisListener) {
		_consumers = consumers;
		_table = table;
		_row = row;
		_countAllItem = countAllItem;
		_rowCounter = rowCounter;
		_job = job;
		_analysisListener = analysisListener;
	}

	@Override
	public void execute() {
		OutcomeSink outcomeSink = new OutcomeSinkImpl();

		int distinctCount = 1;
		if (_countAllItem != null) {
			distinctCount = ((Number) _row.getValue(_countAllItem)).intValue();
		}

		int rowNumber = _rowCounter.addAndGet(distinctCount);
		InputRow inputRow = new MetaModelInputRow(rowNumber, _row);

		for (RowProcessingConsumer consumer : _consumers) {
			boolean process = consumer.satisfiedForConsume(outcomeSink.getOutcomes());

			if (process) {
				if (consumer.isConcurrent()) {
					inputRow = consumer.consume(inputRow, distinctCount, outcomeSink);
				} else {
					synchronized (consumer) {
						inputRow = consumer.consume(inputRow, distinctCount, outcomeSink);
					}
				}
			}
		}
		_analysisListener.rowProcessingProgress(_job, _table, rowNumber);
	}

}
