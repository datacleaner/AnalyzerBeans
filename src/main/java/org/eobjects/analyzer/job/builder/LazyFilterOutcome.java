package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.job.AbstractFilterOutcome;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;

public final class LazyFilterOutcome extends AbstractFilterOutcome implements FilterOutcome {

	private FilterJobBuilder<?, ?> _filterJobBuilder;
	private Enum<?> _category;

	public LazyFilterOutcome(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
		_filterJobBuilder = filterJobBuilder;
		_category = category;
	}

	@Override
	public FilterJob getFilterJob() {
		return _filterJobBuilder.toFilterJob();
	}

	@Override
	public Enum<?> getCategory() {
		return _category;
	}
}
