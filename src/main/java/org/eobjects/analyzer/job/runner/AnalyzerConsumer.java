package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

final class AnalyzerConsumer implements RowProcessingConsumer {

	private final AnalyzerJob _analyzerJob;
	private final AnalyzerBeanInstance _analyzerBeanInstance;
	private final InputColumn<?>[] _inputColumns;

	public AnalyzerConsumer(AnalyzerBeanInstance analyzerBeanInstance, AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns) {
		_analyzerBeanInstance = analyzerBeanInstance;
		_analyzerJob = analyzerJob;
		_inputColumns = inputColumns;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount, FilterOutcomeSink outcomes) {
		RowProcessingAnalyzer<?> analyzer = (RowProcessingAnalyzer<?>) _analyzerBeanInstance.getBean();
		analyzer.run(row, distinctCount);
		return row;
	}

	@Override
	public AnalyzerBeanInstance getBeanInstance() {
		return _analyzerBeanInstance;
	}

	@Override
	public AnalyzerJob getBeanJob() {
		return _analyzerJob;
	}

	@Override
	public String toString() {
		return "AnalyzerConsumer[" + _analyzerBeanInstance + "]";
	}

	@Override
	public FilterOutcome getRequiredOutcome() {
		return _analyzerJob.getRequirement();
	}
}
