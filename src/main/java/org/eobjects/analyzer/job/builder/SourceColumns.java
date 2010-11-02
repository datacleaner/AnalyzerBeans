package org.eobjects.analyzer.job.builder;

import java.util.ArrayList;
import java.util.Collection;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.InputColumnSourceJob;

public final class SourceColumns implements InputColumnSourceJob {

	private final Collection<InputColumn<?>> _sourceColumns = new ArrayList<InputColumn<?>>();

	public SourceColumns(Collection<? extends InputColumn<?>> sourceColumns) {
		_sourceColumns.addAll(sourceColumns);
	}

	@Override
	public InputColumn<?>[] getInput() {
		return new InputColumn<?>[0];
	}

	@Override
	public InputColumn<?>[] getOutput() {
		return _sourceColumns.toArray(new InputColumn<?>[_sourceColumns.size()]);
	}

}
