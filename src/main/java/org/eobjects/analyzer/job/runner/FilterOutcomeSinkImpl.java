package org.eobjects.analyzer.job.runner;

import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.job.FilterOutcome;

final class FilterOutcomeSinkImpl implements FilterOutcomeSink {

	private final Set<FilterOutcome> outcomes = new HashSet<FilterOutcome>();

	@Override
	public void add(FilterOutcome filterOutcome) {
		outcomes.add(filterOutcome);
	}

	@Override
	public boolean contains(FilterOutcome outcome) {
		return outcomes.contains(outcome);
	}
}
