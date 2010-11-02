package org.eobjects.analyzer.job.runner;

import java.util.Arrays;
import java.util.Collection;

import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.Outcome;

public abstract class ConfigurableBeanJobRowProcessingConsumer implements RowProcessingConsumer {

	private final ConfigurableBeanJob<?> _job;
	
	public ConfigurableBeanJobRowProcessingConsumer(ConfigurableBeanJob<?> job) {
		_job = job;
	}
	
	@Override
	public final boolean satisfiedForFlowOrdering(Collection<Outcome> outcomes) {
		if (_job.getRequirement() == null) {
			return true;
		}

		for (Outcome outcome : outcomes) {
			if (outcome.satisfiesRequirement(_job.getRequirement())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean satisfiedForConsume(Outcome[] outcomes) {
		return satisfiedForFlowOrdering(Arrays.asList(outcomes));
	}
}
