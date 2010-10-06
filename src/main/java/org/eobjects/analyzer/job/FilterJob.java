package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

public interface FilterJob extends BeanJob<FilterBeanDescriptor<?, ?>> {

	public FilterOutcome[] getOutcomes();
}
