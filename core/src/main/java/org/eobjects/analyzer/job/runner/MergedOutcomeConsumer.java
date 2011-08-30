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

import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.TransformedInputRow;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MergedOutcomeConsumer extends AbstractOutcomeSinkJobConsumer implements RowProcessingConsumer {

	private static final Logger logger = LoggerFactory.getLogger(MergedOutcomeConsumer.class);
	private final MergedOutcomeJob _mergedOutcomeJob;

	public MergedOutcomeConsumer(MergedOutcomeJob mergedOutcomeJob) {
		super(mergedOutcomeJob);
		_mergedOutcomeJob = mergedOutcomeJob;
	}

	public MergedOutcomeJob getMergedOutcomeJob() {
		return _mergedOutcomeJob;
	}

	@Override
	public boolean isConcurrent() {
		return true;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		Set<InputColumn<?>> columns = new HashSet<InputColumn<?>>();
		MergeInput[] mergeInputs = _mergedOutcomeJob.getMergeInputs();
		for (MergeInput mergeInput : mergeInputs) {
			InputColumn<?>[] inputColumns = mergeInput.getInputColumns();
			for (InputColumn<?> inputColumn : inputColumns) {
				columns.add(inputColumn);
			}
		}
		return columns.toArray(new InputColumn[columns.size()]);
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount, OutcomeSink outcomes) {
		TransformedInputRow result = new TransformedInputRow(row);

		InputColumn<?>[] output = _mergedOutcomeJob.getOutput();
		if (output != null && output.length > 0) {
			MergeInput[] mergeInputs = _mergedOutcomeJob.getMergeInputs();
			MergeInput currentMergeInput = null;
			for (MergeInput mergeInput : mergeInputs) {
				if (outcomes.contains(mergeInput.getOutcome())) {
					currentMergeInput = mergeInput;
					break;
				}
			}

			if (currentMergeInput == null) {
				logger.error(
						"Could not determine current merge input state.\nAvailable outcomes are: {}\nMerged outcomes are: {}",
						outcomes.getOutcomes(), _mergedOutcomeJob.getMergeInputs());
				throw new IllegalStateException("Could not determine current merge input state");
			}

			InputColumn<?>[] inputColumnsForCoalesce = currentMergeInput.getInputColumns();
			for (int i = 0; i < output.length; i++) {
				InputColumn<?> currentColumn = output[i];
				result.addValue(currentColumn, row.getValue(inputColumnsForCoalesce[i]));
			}
		}

		outcomes.add(_mergedOutcomeJob.getOutcome());

		return result;
	}

	@Override
	public AbstractBeanInstance<?> getBeanInstance() {
		// no bean instance available
		return null;
	}

	@Override
	public MergedOutcomeJob getComponentJob() {
		return _mergedOutcomeJob;
	}

	@Override
	public String toString() {
		return "MergedOutcomeConsumer[" + _mergedOutcomeJob + "]";
	}
}