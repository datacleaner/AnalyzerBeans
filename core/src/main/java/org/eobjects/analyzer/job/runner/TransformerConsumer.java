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

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

final class TransformerConsumer extends AbstractRowProcessingConsumer implements RowProcessingConsumer {

    private final AnalysisJob _job;
    private final Transformer<?> _transformer;
    private final TransformerJob _transformerJob;
    private final InputColumn<?>[] _inputColumns;
    private final AnalysisListener _analysisListener;
    private final boolean _concurrent;
    private RowIdGenerator _idGenerator;

    public TransformerConsumer(Transformer<?> transformer, TransformerJob transformerJob,
            InputColumn<?>[] inputColumns, RowProcessingPublishers publishers) {
        super(transformerJob, transformerJob);
        _transformer = transformer;
        _transformerJob = transformerJob;
        _inputColumns = inputColumns;
        if (publishers == null) {
            _job = null;
            _analysisListener = null;
        } else {
            _job = publishers.getAnalysisJob();
            _analysisListener = publishers.getAnalysisListener();
        }

        Concurrent concurrent = _transformerJob.getDescriptor().getAnnotation(Concurrent.class);
        if (concurrent == null) {
            // transformers are by default concurrent
            _concurrent = true;
        } else {
            _concurrent = concurrent.value();
        }
    }

    /**
     * Sets the row id generator to use, when creating new transformed records.
     * 
     * @param idGenerator
     */
    public void setRowIdGenerator(RowIdGenerator idGenerator) {
        _idGenerator = idGenerator;
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
    public Transformer<?> getComponent() {
        return _transformer;
    }

    @Override
    public void consume(final InputRow row, final int distinctCount, final OutcomeSink outcomes,
            final RowProcessingChain chain) {
        final InputColumn<?>[] outputColumns = _transformerJob.getOutput();

        registerListener(_transformer, row, outcomes, chain, outputColumns);

        try {
            final Object[] values = _transformer.transform(row);
            if (values == null) {
                return;
            }
            final TransformedInputRow resultRow;
            if (row instanceof TransformedInputRow) {
                // re-use existing transformed input row.
                resultRow = (TransformedInputRow) row;
            } else {
                resultRow = new TransformedInputRow(row);
            }
            addValuesToRow(resultRow, outputColumns, values);
            chain.processNext(resultRow, distinctCount, outcomes);
        } catch (RuntimeException e) {
            _analysisListener.errorInTransformer(_job, _transformerJob, row, e);
        }

        unregisterListener(_transformer);
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

    private void registerListener(final Transformer<?> transformer, final InputRow row, final OutcomeSink outcomes,
            final RowProcessingChain chain, final InputColumn<?>[] outputColumns) {
        final Set<ProvidedPropertyDescriptor> outputRowCollectorProperties = _transformerJob.getDescriptor()
                .getProvidedPropertiesByType(OutputRowCollector.class);
        if (outputRowCollectorProperties == null || outputRowCollectorProperties.isEmpty()) {
            return;
        }

        final Listener listener = new Listener() {
            private AtomicBoolean first = new AtomicBoolean(true);

            @Override
            public void onValues(Object[] values) {
                boolean isFirst = first.getAndSet(false);
                final TransformedInputRow resultRow;
                if (isFirst) {
                    // retain the first record's id
                    resultRow = new TransformedInputRow(row);
                } else {
                    resultRow = new TransformedInputRow(row, _idGenerator.nextVirtualRowId());
                }

                addValuesToRow(resultRow, outputColumns, values);

                OutcomeSink clonedOutcomeSink = outcomes.clone();
                chain.processNext(resultRow, 1, clonedOutcomeSink);
            }
        };

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
    public TransformerJob getComponentJob() {
        return _transformerJob;
    }

    @Override
    public String toString() {
        return "TransformerConsumer[" + _transformer + "]";
    }
}
