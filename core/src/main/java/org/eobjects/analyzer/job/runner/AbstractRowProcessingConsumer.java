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

import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;

abstract class AbstractRowProcessingConsumer implements RowProcessingConsumer {

	private final OutcomeSinkJob _outcomeSinkJob;
	private final InputColumnSinkJob _inputColumnSinkJob;

	protected AbstractRowProcessingConsumer(OutcomeSinkJob outcomeSinkJob, InputColumnSinkJob inputColumnSinkJob) {
		_outcomeSinkJob = outcomeSinkJob;
		_inputColumnSinkJob = inputColumnSinkJob;
	}

	/**
	 * Ensures that just a single outcome is satisfied
	 */
	@Override
	public final boolean satisfiedForConsume(Outcome[] outcomes, InputRow row) {
		return (satisfiedOutcomesForConsume(outcomes) && satisfiedInputsForConsume(row));
	}

	private boolean satisfiedInputsForConsume(InputRow row) {
		boolean isSatisfiedInputColumns = true;
		InputColumn<?>[] requiredInputColumns = _inputColumnSinkJob.getInput();
		if (requiredInputColumns == null || requiredInputColumns.length == 0) {
			isSatisfiedInputColumns = true;
		} else {
			for (InputColumn<?> inputColumn : requiredInputColumns) {
				if (inputColumn instanceof ExpressionBasedInputColumn) {
					/**
					 * We ignore the ExpressionBasedColumns because they are
					 * artificial columns, don't exist in the row.
					 */
				} else {
					if (!row.containsInputColumn(inputColumn)) {
						isSatisfiedInputColumns = false;
						break;
					}
				}
			}
		}
		return isSatisfiedInputColumns;
	}

	private boolean satisfiedOutcomesForConsume(Outcome[] outcomes) {
		boolean isSatisfiedOutcomes = false;
		Outcome[] requirements = _outcomeSinkJob.getRequirements();
		if (requirements == null || requirements.length == 0) {
			isSatisfiedOutcomes = true;
		} else {
			// each merge input has to be satisfied
			for (Outcome requiredOutcome : requirements) {
				for (Outcome availableOutcome : outcomes) {
					if (availableOutcome.satisfiesRequirement(requiredOutcome)) {
						isSatisfiedOutcomes = true;
						break;
					}
				}
			}
		}
		return isSatisfiedOutcomes;
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
