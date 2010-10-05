package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputRow;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;

class TransformerConsumer implements RowProcessingConsumer {

	private TransformerBeanInstance _transformerBeanInstance;
	private TransformerJob _transformerJob;
	private InputColumn<?>[] _inputColumns;

	public TransformerConsumer(TransformerBeanInstance transformerBeanInstance,
			TransformerJob transformerJob, InputColumn<?>[] inputColumns) {
		_transformerBeanInstance = transformerBeanInstance;
		_transformerJob = transformerJob;
		_inputColumns = inputColumns;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount) {
		MutableInputColumn<?>[] outputColumns = _transformerJob.getOutput();

		Transformer<?> transformer = _transformerBeanInstance.getBean();
		Object[] outputValues = transformer.transform(row);

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
	public TransformerJob getBeanJob() {
		return _transformerJob;
	}

	@Override
	public String toString() {
		return "TransformerConsumer[" + _transformerBeanInstance + "]";
	}
}
