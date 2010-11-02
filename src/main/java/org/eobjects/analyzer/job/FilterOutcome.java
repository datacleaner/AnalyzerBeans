package org.eobjects.analyzer.job;

/**
 * Represents an outcome of a filter.
 * 
 * @see FilterJob
 * 
 * @author Kasper Sørensen
 */
public interface FilterOutcome extends Outcome {

	public FilterJob getFilterJob();

	public Enum<?> getCategory();
}
