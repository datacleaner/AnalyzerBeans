package org.eobjects.analyzer.job;

/**
 * Represents a job that merges separate filter flows into a merged/joined flow.
 * 
 * @author Kasper Sørensen
 */
public interface MergedOutcomeJob extends ComponentJob, InputColumnSourceJob, OutcomeSourceJob {

	/**
	 * @return
	 */
	public MergeInput[] getMergeInputs();

	/**
	 * @return the outcome that represents this merge operation. Succeeding
	 *         components can set up this outcome as a requirement similarly to
	 *         the way that they can have a filter requirement.
	 */
	public MergedOutcome getOutcome();
}
