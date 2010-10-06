package org.eobjects.analyzer.job;


final class LazyFilterOutcome extends AbstractFilterOutcome implements FilterOutcome {

	private FilterJobBuilder<?, ?> _filterJobBuilder;
	private Enum<?> _category;
	
	public static FilterOutcome load(FilterOutcome outcome) {
		if (outcome instanceof LazyFilterOutcome) {
			return new ImmutableFilterOutcome(outcome.getFilterJob(), outcome.getCategory());
		}
		return outcome;
	}

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
