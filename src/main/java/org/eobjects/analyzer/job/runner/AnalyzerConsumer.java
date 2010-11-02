package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

final class AnalyzerConsumer extends ConfigurableBeanJobRowProcessingConsumer implements RowProcessingConsumer {

	private final AnalysisJob _job;
	private final AnalyzerJob _analyzerJob;
	private final AnalyzerBeanInstance _analyzerBeanInstance;
	private final InputColumn<?>[] _inputColumns;
	private final AnalysisListener _analysisListener;

	public AnalyzerConsumer(AnalysisJob job, AnalyzerBeanInstance analyzerBeanInstance, AnalyzerJob analyzerJob,
			InputColumn<?>[] inputColumns, AnalysisListener analysisListener) {
		super(analyzerJob);
		_job = job;
		_analyzerBeanInstance = analyzerBeanInstance;
		_analyzerJob = analyzerJob;
		_inputColumns = inputColumns;
		_analysisListener = analysisListener;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount, OutcomeSink outcomes) {
		RowProcessingAnalyzer<?> analyzer = (RowProcessingAnalyzer<?>) _analyzerBeanInstance.getBean();
		try {
			analyzer.run(row, distinctCount);
		} catch (RuntimeException e) {
			_analysisListener.errorInAnalyzer(_job, _analyzerJob, e);
		}
		return row;
	}

	@Override
	public AnalyzerBeanInstance getBeanInstance() {
		return _analyzerBeanInstance;
	}

	@Override
	public AnalyzerJob getComponentJob() {
		return _analyzerJob;
	}

	@Override
	public String toString() {
		return "AnalyzerConsumer[" + _analyzerBeanInstance + "]";
	}
}
