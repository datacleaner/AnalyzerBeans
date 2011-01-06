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

import dk.eobjects.metamodel.util.BaseObject;

public final class ImmutableMergedOutcomeJob extends BaseObject implements MergedOutcomeJob {

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
	public String toString() {
		return "ImmutableMergedOutcomeJob[name=" + _name + ",mergeInputs=" + _mergeInputs + "]";
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_name);
		identifiers.add(_mergeInputs);
		identifiers.add(_output);
	}
}
