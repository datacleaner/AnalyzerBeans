package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

class AnalyzerConsumer implements RowProcessingConsumer {

	private AnalyzerJob _analyzerJob;
	private AnalyzerBeanInstance _analyzerBeanInstance;
	private InputColumn<?>[] _inputColumns;

	public AnalyzerConsumer(AnalyzerBeanInstance analyzerBeanInstance,
			AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns) {
		_analyzerBeanInstance = analyzerBeanInstance;
		_analyzerJob = analyzerJob;
		_inputColumns = inputColumns;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount) {
		RowProcessingAnalyzer<?> analyzer = (RowProcessingAnalyzer<?>) _analyzerBeanInstance
				.getBean();
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
}
