package org.eobjects.analyzer.job;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

public final class FilterJobBuilder<F extends Filter<C>, C extends Enum<C>> extends
		AbstractBeanWithInputColumnsBuilder<FilterBeanDescriptor<F, C>, F, FilterJobBuilder<F, C>> {

	public FilterJobBuilder(FilterBeanDescriptor<F, C> descriptor) {
		super(descriptor, FilterJobBuilder.class);
	}

	public FilterJob toFilterJob() {
		if (!isConfigured()) {
			throw new IllegalStateException("Filter job is not correctly configured");
		}
		
		return new ImmutableFilterJob(getDescriptor(), new ImmutableBeanConfiguration(getConfiguredProperties()), getRequirement());
	}

	@Override
	public String toString() {
		return "FilterJobBuilder[filter=" + getDescriptor().getDisplayName() + ",inputColumns=" + getInputColumns() + "]";
	}
}
