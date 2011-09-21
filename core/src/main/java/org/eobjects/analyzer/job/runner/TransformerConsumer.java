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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.OutputRowCollector;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.TransformedInputRow;
import org.eobjects.analyzer.descriptors.ProvidedPropertyDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.ThreadLocalOutputRowCollector;
import org.eobjects.analyzer.job.concurrent.ThreadLocalOutputRowCollector.Listener;
import org.eobjects.analyzer.lifecycle.BeanInstance;

final class TransformerConsumer extends AbstractRowProcessingConsumer implements RowProcessingConsumer {

	private final AnalysisJob _job;
	private final BeanInstance<? extends Transformer<?>> _beanInstance;
	private final TransformerJob _transformerJob;
	private final InputColumn<?>[] _inputColumns;
	private final AnalysisListener _analysisListener;
	private final boolean _concurrent;

	public TransformerConsumer(AnalysisJob job, BeanInstance<? extends Transformer<?>> beanInstance,
			TransformerJob transformerJob, InputColumn<?>[] inputColumns, AnalysisListener analysisListener) {
		super(transformerJob, transformerJob);
		_job = job;
		_beanInstance = beanInstance;
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
	public InputRow[] consume(InputRow row, int distinctCount, OutcomeSink outcomes) {
		final InputColumn<?>[] outputColumns = _transformerJob.getOutput();

		final Transformer<?> transformer = _beanInstance.getBean();

		final List<Object[]> outputValues = new ArrayList<Object[]>();

		final Listener listener = new Listener() {
			@Override
			public void onValues(Object[] values) {
				outputValues.add(values);
			}
		};

		registerListener(transformer, listener);

		try {
			outputValues.add(transformer.transform(row));
		} catch (RuntimeException e) {
			_analysisListener.errorInTransformer(_job, _transformerJob, e);
			outputValues.add(new Object[outputColumns.length]);
		}

		unregisterListener(transformer);

		// remove nulls
		for (Iterator<Object[]> iterator = outputValues.iterator(); iterator.hasNext();) {
			Object[] values = iterator.next();
			if (values == null) {
				iterator.remove();
			}
		}

		final List<InputRow> result = new ArrayList<InputRow>();

		if (outputValues.size() == 1) {
			final TransformedInputRow resultRow;
			if (row instanceof TransformedInputRow) {
				// re-use existing transformed input row.
				resultRow = (TransformedInputRow) row;
			} else {
				resultRow = new TransformedInputRow(row);
			}

			final Object[] values = outputValues.get(0);

			assert values != null;

			addValuesToRow(resultRow, outputColumns, values);
			result.add(resultRow);
		} else {
			for (Object[] values : outputValues) {
				final TransformedInputRow resultRow = new TransformedInputRow(row);

				addValuesToRow(resultRow, outputColumns, values);
				result.add(resultRow);
			}
		}

		return result.toArray(new InputRow[result.size()]);
	}

	private void unregisterListener(Transformer<?> transformer) {
		final Set<ProvidedPropertyDescriptor> outputRowCollectorProperties = _transformerJob.getDescriptor()
				.getProvidedPropertiesByType(OutputRowCollector.class);
		for (ProvidedPropertyDescriptor descriptor : outputRowCollectorProperties) {
			OutputRowCollector outputRowCollector = (OutputRowCollector) descriptor.getValue(transformer);
			if (outputRowCollector instanceof ThreadLocalOutputRowCollector) {
				((ThreadLocalOutputRowCollector) outputRowCollector).removeListener();
			}
		}
	}

	private void registerListener(final Transformer<?> transformer, final Listener listener) {
		final Set<ProvidedPropertyDescriptor> outputRowCollectorProperties = _transformerJob.getDescriptor()
				.getProvidedPropertiesByType(OutputRowCollector.class);
		for (ProvidedPropertyDescriptor descriptor : outputRowCollectorProperties) {
			OutputRowCollector outputRowCollector = (OutputRowCollector) descriptor.getValue(transformer);
			if (outputRowCollector instanceof ThreadLocalOutputRowCollector) {
				((ThreadLocalOutputRowCollector) outputRowCollector).setListener(listener);
			} else {
				throw new UnsupportedOperationException("Unsupported output row collector type: " + outputRowCollector);
			}
		}
	}

	private void addValuesToRow(TransformedInputRow resultRow, final InputColumn<?>[] outputColumns, Object[] values) {
		// add output values to row.
		assert outputColumns.length == values.length;
		for (int i = 0; i < outputColumns.length; i++) {
			Object value = values[i];
			InputColumn<?> column = outputColumns[i];
			resultRow.addValue(column, value);
		}
	}

	@Override
	public BeanInstance<? extends Transformer<?>> getBeanInstance() {
		return _beanInstance;
	}

	@Override
	public TransformerJob getComponentJob() {
		return _transformerJob;
	}

	@Override
	public String toString() {
		return "TransformerConsumer[" + _beanInstance + "]";
	}
}
