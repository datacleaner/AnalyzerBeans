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
package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.Concurrent;
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
	private final boolean _concurrent;

	public TransformerConsumer(AnalysisJob job, TransformerBeanInstance transformerBeanInstance,
			TransformerJob transformerJob, InputColumn<?>[] inputColumns, AnalysisListener analysisListener) {
		super(transformerJob);
		_job = job;
		_transformerBeanInstance = transformerBeanInstance;
		_transformerJob = transformerJob;
		_inputColumns = inputColumns;
		_analysisListener = analysisListener;

		Concurrent concurrent = _transformerJob.getDescriptor().getAnnotation(Concurrent.class);
		if (concurrent == null) {
			// transformers are by default concurrent
			_concurrent = true;
		} else {
			_concurrent = concurrent.value();
		}
	}

	@Override
	public boolean isConcurrent() {
		return _concurrent;
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
