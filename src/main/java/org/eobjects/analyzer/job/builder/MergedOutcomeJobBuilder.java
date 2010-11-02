package org.eobjects.analyzer.job.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.ImmutableMergedOutcomeJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;

public final class MergedOutcomeJobBuilder implements InputColumnSourceJob {

	private final List<MergeInputBuilder> _mergeInputs = new ArrayList<MergeInputBuilder>();
	private final List<MutableInputColumn<?>> _outputColumns = new ArrayList<MutableInputColumn<?>>();
	private final IdGenerator _idGenerator;

	public MergedOutcomeJobBuilder(IdGenerator idGenerator) {
		_idGenerator = idGenerator;
	}

	public MergeInputBuilder addMergedOutcome(FilterJobBuilder<?, ?> fjb, Enum<?> category) {
		MergeInputBuilder mib = new MergeInputBuilder(fjb, category);
		_mergeInputs.add(mib);
		return mib;
	}

	public MergeInputBuilder addMergedOutcome(Outcome outcome) {
		MergeInputBuilder mib = new MergeInputBuilder(outcome);
		_mergeInputs.add(mib);
		return mib;
	}

	public MergedOutcomeJobBuilder removeMergeInput(MergeInputBuilder mib) {
		_mergeInputs.remove(mib);
		return this;
	}

	public List<MergeInputBuilder> getMergeInputs() {
		return Collections.unmodifiableList(_mergeInputs);
	}

	public MergedOutcomeJob toMergedOutcomeJob() throws IllegalStateException {
		if (_mergeInputs.isEmpty()) {
			throw new IllegalStateException("Merged outcome jobs need at least 2 merged outcomes, none found");
		}
		if (_mergeInputs.size() == 1) {
			throw new IllegalStateException("Merged outcome jobs need at least 2 merged outcomes, only 1 found");
		}

		List<MergeInput> mergeInputs = new ArrayList<MergeInput>();

		for (MergeInputBuilder mib : _mergeInputs) {
			MergeInput mergeInput = mib.toMergeInput();
			mergeInputs.add(mergeInput);
		}

		return new ImmutableMergedOutcomeJob(mergeInputs, getOutputColumns());
	}

	public List<MutableInputColumn<?>> getOutputColumns() {
		for (MergeInputBuilder mib : _mergeInputs) {
			List<InputColumn<?>> inputColumns = mib.getInputColumns();
			int numInput = inputColumns.size();
			int numOutput = _outputColumns.size();
			if (numInput > numOutput) {
				for (int i = numOutput; i < numInput; i++) {
					_outputColumns.add(new TransformedInputColumn<Object>("Merged column " + (i + 1), inputColumns.get(i)
							.getDataTypeFamily(), _idGenerator));
				}
			} else if (numInput < numOutput) {
				for (int i = numOutput; i > numInput; i--) {
					_outputColumns.remove(i - 1);
				}
			}
		}

		return Collections.unmodifiableList(_outputColumns);
	}

	@Override
	public InputColumn<?>[] getInput() {
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		List<MergeInputBuilder> mergeInputs = getMergeInputs();
		for (MergeInputBuilder mib : mergeInputs) {
			List<InputColumn<?>> inputColumns = mib.getInputColumns();
			for (InputColumn<?> inputColumn : inputColumns) {
				if (!result.contains(inputColumn)) {
					result.add(inputColumn);
				}
			}
		}
		return result.toArray(new InputColumn<?>[0]);
	}

	@Override
	public MutableInputColumn<?>[] getOutput() {
		return getOutputColumns().toArray(new MutableInputColumn<?>[0]);
	}
}
