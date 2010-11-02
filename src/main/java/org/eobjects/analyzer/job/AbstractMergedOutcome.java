package org.eobjects.analyzer.job;

import java.util.Arrays;
import java.util.List;

import dk.eobjects.metamodel.util.BaseObject;

public abstract class AbstractMergedOutcome extends BaseObject implements MergedOutcome {

	@Override
	protected final boolean classEquals(BaseObject obj) {
		// should work with all subtypes
		return obj instanceof MergedOutcome;
	}

	@Override
	protected final void decorateIdentity(List<Object> identifiers) {
		identifiers.add(getMergedOutcomeJob());
	}

	@Override
	public String toString() {
		return "MergedOutcome[output=" + Arrays.toString(getMergedOutcomeJob().getOutput()) + "]";
	}
}
