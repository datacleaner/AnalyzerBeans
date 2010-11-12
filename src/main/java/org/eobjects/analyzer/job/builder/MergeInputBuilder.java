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
