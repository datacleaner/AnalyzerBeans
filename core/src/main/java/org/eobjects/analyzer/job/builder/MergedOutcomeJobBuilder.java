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
package org.eobjects.analyzer.job.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.ImmutableMergedOutcomeJob;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.LazyMergedOutcome;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.job.OutcomeSourceJob;

public final class MergedOutcomeJobBuilder implements InputColumnSourceJob, InputColumnSinkJob, OutcomeSourceJob,
		OutcomeSinkJob {

	private final List<MergeInputBuilder> _mergeInputs = new ArrayList<MergeInputBuilder>();
	private final List<MutableInputColumn<?>> _outputColumns = new ArrayList<MutableInputColumn<?>>();
	private final IdGenerator _idGenerator;
	private volatile String _name;

	// We keep a cached version of the resulting filter job because of
	// references coming from other objects, particular LazyFilterOutcome.
	private volatile MergedOutcomeJob _cachedJob;

	public MergedOutcomeJobBuilder(IdGenerator idGenerator) {
		_idGenerator = idGenerator;
	}

	public MergeInputBuilder addMergedOutcome(FilterJobBuilder<?, ?> fjb, Enum<?> category) {
		MergeInputBuilder mib = new MergeInputBuilder(fjb, category);
		_mergeInputs.add(mib);
		return mib;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
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

	public MergedOutcomeJob toMergedOutcomeJob(boolean validate) {
		if (validate && _mergeInputs.isEmpty()) {
			throw new IllegalStateException("Merged outcome jobs need at least 2 merged outcomes, none found");
		}
		if (validate && _mergeInputs.size() == 1) {
			throw new IllegalStateException("Merged outcome jobs need at least 2 merged outcomes, only 1 found");
		}

		List<MergeInput> mergeInputs = new ArrayList<MergeInput>();

		for (MergeInputBuilder mib : _mergeInputs) {
			MergeInput mergeInput = mib.toMergeInput();
			mergeInputs.add(mergeInput);
		}

		ImmutableMergedOutcomeJob job = new ImmutableMergedOutcomeJob(getName(), mergeInputs, getOutputColumns());
		if (_cachedJob == null) {
			_cachedJob = job;
		} else {
			if (!_cachedJob.equals(job)) {
				_cachedJob = job;
			}
		}
		return _cachedJob;
	}

	public MergedOutcomeJob toMergedOutcomeJob() throws IllegalStateException {
		return toMergedOutcomeJob(true);
	}

	public List<MutableInputColumn<?>> getOutputColumns() {
		for (MergeInputBuilder mib : _mergeInputs) {
			List<InputColumn<?>> inputColumns = mib.getInputColumns();
			int numInput = inputColumns.size();
			int numOutput = _outputColumns.size();
			if (numInput > numOutput) {
				for (int i = numOutput; i < numInput; i++) {
					InputColumn<?> inputColumn = inputColumns.get(i);
					TransformedInputColumn<Object> outputColumn = new TransformedInputColumn<Object>("Merged column " + (i + 1), _idGenerator);
					outputColumn.setDataTypeFamily(inputColumn.getDataTypeFamily());
					outputColumn.setDataType(inputColumn.getDataType());
					_outputColumns.add(outputColumn);
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

	@Override
	public Outcome[] getRequirements() {
		List<Outcome> result = new ArrayList<Outcome>(_mergeInputs.size());
		for (MergeInputBuilder mergeInputBuilder : _mergeInputs) {
			result.add(mergeInputBuilder.getOutcome());
		}
		return result.toArray(new Outcome[result.size()]);
	}

	@Override
	public Outcome[] getOutcomes() {
		return new Outcome[] { new LazyMergedOutcome(this) };
	}
}
