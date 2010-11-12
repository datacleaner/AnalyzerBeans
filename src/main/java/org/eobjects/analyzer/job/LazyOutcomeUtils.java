package org.eobjects.analyzer.job;

import org.eobjects.analyzer.job.builder.LazyFilterOutcome;

public final class LazyOutcomeUtils {

	private LazyOutcomeUtils() {
		// prevent instantiation
	}

	public static Outcome load(Outcome outcome) {
		if (outcome instanceof LazyFilterOutcome) {
			LazyFilterOutcome lfo = (LazyFilterOutcome) outcome;
			return new ImmutableFilterOutcome(lfo.getFilterJob(), lfo.getCategory());
		} else if (outcome instanceof LazyMergedOutcome) {
			MergedOutcomeJob job = ((LazyMergedOutcome) outcome).getMergedOutcomeJob();
			return new ImmutableMergedOutcome(job);
		}
		return outcome;
	}
}
