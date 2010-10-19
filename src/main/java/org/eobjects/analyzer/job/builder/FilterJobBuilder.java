package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.ImmutableFilterJob;

public final class FilterJobBuilder<F extends Filter<C>, C extends Enum<C>> extends
		AbstractBeanWithInputColumnsBuilder<FilterBeanDescriptor<F, C>, F, FilterJobBuilder<F, C>> {

	// We keep a cached version of the resulting filter job because of
	// references coming from other objects, particular LazyFilterOutcome.
	private FilterJob _cachedFilterJob;

	public FilterJobBuilder(FilterBeanDescriptor<F, C> descriptor) {
		super(descriptor, FilterJobBuilder.class);
	}

	public FilterJob toFilterJob() {
		if (!isConfigured()) {
			throw new IllegalStateException("Filter job is not correctly configured");
		}

		if (_cachedFilterJob == null) {
			_cachedFilterJob = new ImmutableFilterJob(getDescriptor(), new ImmutableBeanConfiguration(
					getConfiguredProperties()), getRequirement());
		} else {
			ImmutableFilterJob newFilterJob = new ImmutableFilterJob(getDescriptor(), new ImmutableBeanConfiguration(
					getConfiguredProperties()), getRequirement());
			if (!newFilterJob.equals(_cachedFilterJob)) {
				_cachedFilterJob = newFilterJob;
			}
		}
		return _cachedFilterJob;
	}

	@Override
	public String toString() {
		return "FilterJobBuilder[filter=" + getDescriptor().getDisplayName() + ",inputColumns=" + getInputColumns() + "]";
	}
}
