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

import java.util.Collection;

import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;

abstract class AbstractOutcomeSinkJobConsumer implements RowProcessingConsumer {

	private final OutcomeSinkJob _outcomeSinkJob;

	protected AbstractOutcomeSinkJobConsumer(OutcomeSinkJob outcomeSinkJob) {
		_outcomeSinkJob = outcomeSinkJob;
	}

	/**
	 * Ensures that just a single outcome is satisfied
	 */
	@Override
	public final boolean satisfiedForConsume(Outcome[] outcomes) {
		Outcome[] requirements = _outcomeSinkJob.getRequirements();
		if (requirements == null || requirements.length == 0) {
			return true;
		}

		// each merge input has to be satisfied
		for (Outcome requiredOutcome : requirements) {
			for (Outcome availableOutcome : outcomes) {
				if (availableOutcome.satisfiesRequirement(requiredOutcome)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Ensures that ALL outcomes are available
	 */
	@Override
	public final boolean satisfiedForFlowOrdering(Collection<Outcome> outcomes) {
		Outcome[] requirements = _outcomeSinkJob.getRequirements();
		if (requirements == null || requirements.length == 0) {
			return true;
		}

		// each outcome has to be satisfied
		for (Outcome requiredOutcome : requirements) {
			boolean found = false;
			for (Outcome availableOutcome : outcomes) {
				if (availableOutcome.satisfiesRequirement(requiredOutcome)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
