package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;

public interface MergeInput {

	public InputColumn<?>[] getInputColumns();
	
	public Outcome getOutcome();
}
