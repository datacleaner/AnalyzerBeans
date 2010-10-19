package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.data.InputColumn;

public interface SourceColumnChangeListener {

	public void onAdd(InputColumn<?> sourceColumn);
	
	public void onRemove(InputColumn<?> sourceColumn);
}
