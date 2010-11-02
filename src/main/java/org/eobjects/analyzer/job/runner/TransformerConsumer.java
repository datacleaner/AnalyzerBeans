package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.TransformedInputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;

final class TransformerConsumer extends ConfigurableBeanJobRowProcessingConsumer implements RowProcessingConsumer {

	private final AnalysisJob _job;
	private final TransformerBeanInstance _transformerBeanInstance;
	private final TransformerJob _transformerJob;
	private final InputColumn<?>[] _inputColumns;
	private final AnalysisListener _analysisListener;

	public TransformerConsumer(AnalysisJob job, TransformerBeanInstance transformerBeanInstance,
			TransformerJob transformerJob, InputColumn<?>[] inputColumns, AnalysisListener analysisListener) {
		super(transformerJob);
		_job = job;
		_transformerBeanInstance = transformerBeanInstance;
		_transformerJob = transformerJob;
		_inputColumns = inputColumns;
		_analysisListener = analysisListener;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount, OutcomeSink outcomes) {
		InputColumn<?>[] outputColumns = _transformerJob.getOutput();

		Transformer<?> transformer = _transformerBeanInstance.getBean();

		Object[] outputValues;
		try {
			outputValues = transformer.transform(row);
		} catch (RuntimeException e) {
			_analysisListener.errorInTransformer(_job, _transformerJob, e);
			outputValues = new Object[outputColumns.length];
		}

		assert outputColumns.length == outputValues.length;

		TransformedInputRow result = new TransformedInputRow(row);
		for (int i = 0; i < outputColumns.length; i++) {
			result.addValue(outputColumns[i], outputValues[i]);
		}

		return result;
	}

	@Override
	public TransformerBeanInstance getBeanInstance() {
		return _transformerBeanInstance;
	}

	@Override
	public TransformerJob getComponentJob() {
		return _transformerJob;
	}

	@Override
	public String toString() {
		return "TransformerConsumer[" + _transformerBeanInstance + "]";
	}
}
