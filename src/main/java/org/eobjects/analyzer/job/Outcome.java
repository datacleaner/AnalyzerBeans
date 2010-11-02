package org.eobjects.analyzer.job;

/**
 * Represents the outcome of a filtering flow. An outcome can be used as a
 * requirement for succeeding components in order to make them conditional based
 * on filters.
 * 
 * @see FilterJob
 * @see MergedOutcomeJob
 * 
 * @author Kasper Sørensen
 */
public interface Outcome {

	/**
	 * Requests whether or not a specific requirement is satisfied by this
	 * outcome
	 * 
	 * @param requirement
	 * @return
	 */
	public boolean satisfiesRequirement(Outcome requirement);
}
