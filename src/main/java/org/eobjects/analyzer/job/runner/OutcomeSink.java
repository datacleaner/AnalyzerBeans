package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.Outcome;

/**
 * A simple write-only interface for RowProcessingConsumers to add outcomes to.
 * 
 * @author Kasper Sørensen
 */
public interface OutcomeSink {

	public void add(Outcome filterOutcome);

	public Outcome[] getOutcomes();

	public boolean contains(Outcome outcome);
}
