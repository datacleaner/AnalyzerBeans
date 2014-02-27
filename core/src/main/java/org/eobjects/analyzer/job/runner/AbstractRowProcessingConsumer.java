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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.util.SourceColumnFinder;

/**
 * Abstract implementation of {@link RowProcessingConsumer}. Contains utility
 * methods to help make the 'is satisfied for execution' methods easier to
 * implement.
 */
abstract class AbstractRowProcessingConsumer implements RowProcessingConsumer {

    private final OutcomeSinkJob _outcomeSinkJob;
    private final Set<OutcomeSinkJob> _sourceJobsOfInputColumns;

    protected AbstractRowProcessingConsumer(OutcomeSinkJob outcomeSinkJob, InputColumnSinkJob inputColumnSinkJob,
            RowProcessingPublishers publishers) {
        this(outcomeSinkJob, inputColumnSinkJob, publishers.getSourceColumnFinder());
    }

    protected AbstractRowProcessingConsumer(OutcomeSinkJob outcomeSinkJob, InputColumnSinkJob inputColumnSinkJob,
            SourceColumnFinder sourceColumnFinder) {
        this(outcomeSinkJob, buildSourceJobsOfInputColumns(inputColumnSinkJob, sourceColumnFinder));
    }
    
    protected AbstractRowProcessingConsumer(OutcomeSinkJob outcomeSinkJob, Set<OutcomeSinkJob> sourceJobsOfInputColumns) {
        _outcomeSinkJob = outcomeSinkJob;
        _sourceJobsOfInputColumns = sourceJobsOfInputColumns;
    }

    private static Set<OutcomeSinkJob> buildSourceJobsOfInputColumns(InputColumnSinkJob inputColumnSinkJob,
            SourceColumnFinder sourceColumnFinder) {
        Set<OutcomeSinkJob> result = new HashSet<OutcomeSinkJob>();
        Set<Object> sourceJobsOfInputColumns = sourceColumnFinder.findAllSourceJobs(inputColumnSinkJob);
        for (Iterator<Object> it = sourceJobsOfInputColumns.iterator(); it.hasNext();) {
            Object sourceJob = it.next();
            if (sourceJob instanceof OutcomeSinkJob) {
                OutcomeSinkJob sourceOutcomeSinkJob = (OutcomeSinkJob) sourceJob;
                Outcome[] requirements = sourceOutcomeSinkJob.getRequirements();
                if (requirements != null && requirements.length > 0) {
                    result.add(sourceOutcomeSinkJob);
                }
            }
        }
        return result;
    }

    /**
     * Ensures that just a single outcome is satisfied
     */
    @Override
    public final boolean satisfiedForConsume(Outcome[] outcomes, InputRow row) {
        boolean satisfiedOutcomesForConsume = satisfiedOutcomesForConsume(_outcomeSinkJob, outcomes);
        if (!satisfiedOutcomesForConsume) {
            return false;
        }
        boolean satisfiedInputsForConsume = satisfiedInputsForConsume(row, outcomes);
        return satisfiedInputsForConsume;
    }

    @Override
    public InputColumn<?>[] getOutputColumns() {
        return new InputColumn[0];
    }

    private boolean satisfiedInputsForConsume(InputRow row, Outcome[] outcomes) {
        if (_sourceJobsOfInputColumns.isEmpty()) {
            return true;
        }

        for (Object sourceJobsOfInputColumn : _sourceJobsOfInputColumns) {
            // if any of the source jobs is satisfied, then continue
            if (sourceJobsOfInputColumn instanceof OutcomeSinkJob) {
                OutcomeSinkJob outcomeSinkJob = (OutcomeSinkJob) sourceJobsOfInputColumn;
                boolean satisfiedOutcomesForConsume = satisfiedOutcomesForConsume(outcomeSinkJob, outcomes);
                if (satisfiedOutcomesForConsume) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean satisfiedOutcomesForConsume(OutcomeSinkJob outcomeSinkJob, Outcome[] outcomes) {
        boolean isSatisfiedOutcomes = false;
        Outcome[] requirements = outcomeSinkJob.getRequirements();
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
