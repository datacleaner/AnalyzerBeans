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
import org.eobjects.analyzer.job.ImmutableMergeInput;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.Outcome;

public final class MergeInputBuilder {

	private final List<InputColumn<?>> _inputColumns = new ArrayList<InputColumn<?>>();
	private final Outcome _outcome;

	public MergeInputBuilder(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
		_outcome = new LazyFilterOutcome(filterJobBuilder, category);
	}
	
	public Outcome getOutcome() {
		return _outcome;
	}

	public MergeInputBuilder(Outcome outcome) {
		_outcome = outcome;
	}

	public MergeInputBuilder addInputColumn(InputColumn<?> inputColumn) {
		_inputColumns.add(inputColumn);
		return this;
	}

	public MergeInputBuilder removeInputColumn(InputColumn<?> inputColumn) {
		_inputColumns.remove(inputColumn);
		return this;
	}

	public List<InputColumn<?>> getInputColumns() {
		return Collections.unmodifiableList(_inputColumns);
	}

	public MergeInput toMergeInput() {
		return new ImmutableMergeInput(_inputColumns, _outcome);
	}
}
