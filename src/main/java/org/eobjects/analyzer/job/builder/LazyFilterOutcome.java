package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.job.AbstractFilterOutcome;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.ImmutableFilterOutcome;
import org.eobjects.analyzer.job.Outcome;

public final class LazyFilterOutcome extends AbstractFilterOutcome implements FilterOutcome {

	private FilterJobBuilder<?, ?> _filterJobBuilder;
	private Enum<?> _category;

	public static Outcome load(Outcome outcome) {
		if (outcome instanceof LazyFilterOutcome) {
			LazyFilterOutcome lfo = (LazyFilterOutcome) outcome;
			return new ImmutableFilterOutcome(lfo.getFilterJob(), lfo.getCategory());
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
