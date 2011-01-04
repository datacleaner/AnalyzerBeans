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
package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;

public final class ImmutableMergedOutcomeJob implements MergedOutcomeJob {

	private final String _name;
	private final List<MergeInput> _mergeInputs;
	private final MergedOutcome _outcome;
	private final List<MutableInputColumn<?>> _output;

	public ImmutableMergedOutcomeJob(String name, Collection<MergeInput> input, Collection<MutableInputColumn<?>> output) {
		_name = name;
		_mergeInputs = new ArrayList<MergeInput>(input);
		_output = new ArrayList<MutableInputColumn<?>>(output);
		_outcome = new ImmutableMergedOutcome(this);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public MergeInput[] getMergeInputs() {
		return _mergeInputs.toArray(new MergeInput[_mergeInputs.size()]);
	}

	@Override
	public MergedOutcome getOutcome() {
		return _outcome;
	}

	@Override
	public Outcome[] getOutcomes() {
		return new Outcome[] { getOutcome() };
	}

	@Override
	public MutableInputColumn<?>[] getOutput() {
		return _output.toArray(new MutableInputColumn<?>[_output.size()]);
	}

	@Override
	public InputColumn<?>[] getInput() {
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();

		for (MergeInput mergeInput : _mergeInputs) {
			InputColumn<?>[] inputColumns = mergeInput.getInputColumns();
			for (InputColumn<?> inputColumn : inputColumns) {
				if (!result.contains(inputColumn)) {
					result.add(inputColumn);
				}
			}
		}

		return result.toArray(new InputColumn<?>[result.size()]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + ((_mergeInputs == null) ? 0 : _mergeInputs.hashCode());
		result = prime * result + ((_output == null) ? 0 : _output.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImmutableMergedOutcomeJob other = (ImmutableMergedOutcomeJob) obj;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		if (_mergeInputs == null) {
			if (other._mergeInputs != null)
				return false;
		} else if (!_mergeInputs.equals(other._mergeInputs))
			return false;
		if (_outcome == null) {
			if (other._outcome != null)
				return false;
		} else if (!_outcome.equals(other._outcome))
			return false;
		if (_output == null) {
			if (other._output != null)
				return false;
		} else if (!_output.equals(other._output))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ImmutableMergedOutcomeJob[mergeInputs=");
		sb.append(_mergeInputs);
		sb.append("]");
		return sb.toString();
	}
}
