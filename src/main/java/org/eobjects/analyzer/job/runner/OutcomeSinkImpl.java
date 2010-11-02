package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.job.Outcome;

public final class OutcomeSinkImpl implements OutcomeSink {

	private final List<Outcome> outcomes = new ArrayList<Outcome>();

	@Override
	public void add(Outcome filterOutcome) {
		outcomes.add(filterOutcome);
	}

	@Override
	public boolean contains(Outcome outcome) {
		return outcomes.contains(outcome);
	}

	@Override
	public Outcome[] getOutcomes() {
		return outcomes.toArray(new Outcome[outcomes.size()]);
	}

	@Override
	public String toString() {
		return "OutcomeSink[" + outcomes + "]";
	}
}
