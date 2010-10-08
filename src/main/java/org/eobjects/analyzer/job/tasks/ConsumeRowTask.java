package org.eobjects.analyzer.job.tasks;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.FilterOutcomeSink;
import org.eobjects.analyzer.job.runner.FilterOutcomeSinkImpl;
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
	private final CompletionListener _completionListener;

	public ConsumeRowTask(Iterable<RowProcessingConsumer> consumers, Table table, Row row, SelectItem countAllItem,
			AtomicInteger rowCounter, AnalysisJob job, AnalysisListener analysisListener,
			CompletionListener completionListener) {
		_consumers = consumers;
		_table = table;
		_row = row;
		_countAllItem = countAllItem;
		_rowCounter = rowCounter;
		_job = job;
		_analysisListener = analysisListener;
		_completionListener = completionListener;
	}

	@Override
	public void execute() {
		FilterOutcomeSink outcomes = new FilterOutcomeSinkImpl();
		InputRow inputRow = new MetaModelInputRow(_row);

		int distinctCount = 1;
		if (_countAllItem != null) {
			distinctCount = ((Number) _row.getValue(_countAllItem)).intValue();
		}

		int rowNumber = _rowCounter.addAndGet(distinctCount);

		for (RowProcessingConsumer rowProcessingConsumer : _consumers) {
			FilterOutcome requiredOutcome = rowProcessingConsumer.getRequiredOutcome();
			boolean process;
			if (requiredOutcome == null) {
				process = true;
			} else {
				process = outcomes.contains(requiredOutcome);
			}

			if (process) {
				synchronized (rowProcessingConsumer) {
					inputRow = rowProcessingConsumer.consume(inputRow, distinctCount, outcomes);
				}
			}
		}
		_analysisListener.rowProcessingProgress(_job, _table, rowNumber);
		_completionListener.onComplete();
	}

}
