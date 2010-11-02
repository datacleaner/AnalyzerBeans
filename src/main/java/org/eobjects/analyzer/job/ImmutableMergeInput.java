package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;

public final class ImmutableMergeInput implements MergeInput {

	private final List<InputColumn<?>> _inputColumns;
	private final Outcome _outcome;

	public ImmutableMergeInput(Collection<InputColumn<?>> inputColumns, Outcome outcome) {
		_inputColumns = new ArrayList<InputColumn<?>>(inputColumns);
		_outcome = outcome;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_inputColumns == null) ? 0 : _inputColumns.hashCode());
		result = prime * result + ((_outcome == null) ? 0 : _outcome.hashCode());
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
		ImmutableMergeInput other = (ImmutableMergeInput) obj;
		if (_inputColumns == null) {
			if (other._inputColumns != null)
				return false;
		} else if (!_inputColumns.equals(other._inputColumns))
			return false;
		if (_outcome == null) {
			if (other._outcome != null)
				return false;
		} else if (!_outcome.equals(other._outcome))
			return false;
		return true;
	}
}
