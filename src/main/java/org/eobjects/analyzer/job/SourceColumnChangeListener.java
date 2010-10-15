package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;

public interface SourceColumnChangeListener {

	public void onAdd(InputColumn<?> sourceColumn);
	
	public void onRemove(InputColumn<?> sourceColumn);
}
