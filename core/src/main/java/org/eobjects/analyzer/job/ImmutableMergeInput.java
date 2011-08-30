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

import org.eobjects.metamodel.util.BaseObject;

public final class ImmutableMergeInput extends BaseObject implements MergeInput {

	private final List<InputColumn<?>> _inputColumns;
	private final Outcome _outcome;

	public ImmutableMergeInput(Collection<InputColumn<?>> inputColumns, Outcome outcome) {
		_inputColumns = new ArrayList<InputColumn<?>>(inputColumns);
		_outcome = LazyOutcomeUtils.load(outcome);
	}

	@Override
	public InputColumn<?>[] getInputColumns() {
		return _inputColumns.toArray(new InputColumn<?>[_inputColumns.size()]);
	}

	@Override
	public Outcome getOutcome() {
		return _outcome;
	}

	@Override
	public String toString() {
		return "ImmutableMergeInput[" + _outcome + "]";
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_inputColumns);
		identifiers.add(_outcome);
	}
}
