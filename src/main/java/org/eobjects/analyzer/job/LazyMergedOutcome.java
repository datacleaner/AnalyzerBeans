package org.eobjects.analyzer.job;

import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;

public final class LazyMergedOutcome extends AbstractMergedOutcome implements MergedOutcome {

	private MergedOutcomeJobBuilder _mergedOutcomeJobBuilder;

	public LazyMergedOutcome(MergedOutcomeJobBuilder mergedOutcomeJobBuilder) {
		_mergedOutcomeJobBuilder = mergedOutcomeJobBuilder;
	}
	
	public MergedOutcomeJobBuilder getBuilder() {
		return _mergedOutcomeJobBuilder;
	}

	@Override
	public boolean satisfiesRequirement(Outcome requirement) {
		return new ImmutableMergedOutcome(getMergedOutcomeJob()).satisfiesRequirement(requirement);
	}

	@Override
	public MergedOutcomeJob getMergedOutcomeJob() {
		return _mergedOutcomeJobBuilder.toMergedOutcomeJob();
	}
}
