package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

/**
 * Represents a job that filters/categorizes incoming rows.
 * 
 * @see Filter
 * @See FilterBean
 * 
 * @author Kasper Sørensen
 */
public interface FilterJob extends ConfigurableBeanJob<FilterBeanDescriptor<?, ?>>, OutcomeSourceJob {

	public FilterOutcome[] getOutcomes();
}
