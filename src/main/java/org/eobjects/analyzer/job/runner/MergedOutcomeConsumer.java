package org.eobjects.analyzer.job.runner;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.TransformedInputRow;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MergedOutcomeConsumer implements RowProcessingConsumer {

	private static final Logger logger = LoggerFactory.getLogger(MergedOutcomeConsumer.class);
	private final MergedOutcomeJob _mergedOutcomeJob;

	public MergedOutcomeConsumer(MergedOutcomeJob mergedOutcomeJob) {
		_mergedOutcomeJob = mergedOutcomeJob;
	}

	public MergedOutcomeJob getMergedOutcomeJob() {
		return _mergedOutcomeJob;
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

	/**
	 * Ensures that just a single merge input is satisfied
	 */
	@Override
	public boolean satisfiedForConsume(Outcome[] outcomes) {
		MergeInput[] mergeInputs = _mergedOutcomeJob.getMergeInputs();

		// each merge input has to be satisfied
		for (MergeInput mergeInput : mergeInputs) {
			Outcome requiredOutcome = mergeInput.getOutcome();
			for (Outcome availableOutcome : outcomes) {
				if (availableOutcome.satisfiesRequirement(requiredOutcome)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Ensures that ALL merge inputs are satisfied
	 */
	@Override
	public boolean satisfiedForFlowOrdering(Collection<Outcome> outcomes) {
		MergeInput[] mergeInputs = _mergedOutcomeJob.getMergeInputs();

		// each merge input has to be satisfied
		for (MergeInput mergeInput : mergeInputs) {
			Outcome requiredOutcome = mergeInput.getOutcome();
			boolean found = false;
			for (Outcome availableOutcome : outcomes) {
				if (availableOutcome.satisfiesRequirement(requiredOutcome)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
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

}