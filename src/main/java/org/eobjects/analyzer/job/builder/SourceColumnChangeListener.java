package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.data.InputColumn;

/**
 * Listener interface for receiving notifications when source columns are being
 * added or removed from the job.
 * 
 * Note that other columns than source columns may be of interest since
 * transformers generate virtual columns as well. Use a
 * TransformerChangeListener to receive notifications about such columns.
 * 
 * @see TransformerChangeListener
 * 
 * @author Kasper Sørensen
 */
public interface SourceColumnChangeListener {

	public void onAdd(InputColumn<?> sourceColumn);

	public void onRemove(InputColumn<?> sourceColumn);
}
