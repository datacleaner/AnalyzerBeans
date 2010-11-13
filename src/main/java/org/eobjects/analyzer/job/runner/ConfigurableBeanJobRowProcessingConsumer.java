/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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
