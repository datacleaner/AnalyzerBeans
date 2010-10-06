package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.FilterOutcome;

/**
 * A simple write-only interface for RowProcessingConsumers to add filter
 * outcomes to.
 * 
 * @author Kasper Sørensen
 */
interface FilterOutcomeSink {

	public void add(FilterOutcome filterOutcome);

	public boolean contains(FilterOutcome outcome);
}
