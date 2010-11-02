package org.eobjects.analyzer.job;


public class ImmutableMergedOutcome extends AbstractMergedOutcome implements MergedOutcome {

	private final MergedOutcomeJob _mergedOutcomeJob;

	public ImmutableMergedOutcome(MergedOutcomeJob mergedOutcomeJob) {
		_mergedOutcomeJob = mergedOutcomeJob;
	}

	@Override
	public MergedOutcomeJob getMergedOutcomeJob() {
		return _mergedOutcomeJob;
	}

	@Override
	public boolean satisfiesRequirement(Outcome requirement) {
		if (requirement.equals(this)) {
			return true;
		}

		MergeInput[] mergeInputs = _mergedOutcomeJob.getMergeInputs();
		for (MergeInput mergeInput : mergeInputs) {
			if (mergeInput.getOutcome().satisfiesRequirement(requirement)) {
				return true;
			}
		}
		return false;
	}
}
