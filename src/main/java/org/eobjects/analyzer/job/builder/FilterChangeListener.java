package org.eobjects.analyzer.job.builder;


/**
 * Listener interface for receiving notifications when filters are being added
 * or removed from an analysis job.
 * 
 * @author Kasper Sørensen
 */
public interface FilterChangeListener {

	public void onAdd(FilterJobBuilder<?, ?> filterJobBuilder);

	public void onRemove(FilterJobBuilder<?, ?> filterJobBuilder);

}
