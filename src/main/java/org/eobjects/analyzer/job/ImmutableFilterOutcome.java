package org.eobjects.analyzer.job;

public final class ImmutableFilterOutcome extends AbstractFilterOutcome implements FilterOutcome {

	private final FilterJob _filterJob;
	private final Enum<?> _category;

	public ImmutableFilterOutcome(FilterJob filterJob, Enum<?> category) {
		_filterJob = filterJob;
		_category = category;
	}

	@Override
	public FilterJob getFilterJob() {
		return _filterJob;
	}

	@Override
	public Enum<?> getCategory() {
		return _category;
	}
}
