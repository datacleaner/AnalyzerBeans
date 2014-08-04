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
package org.eobjects.analyzer.job;

import java.util.Arrays;
import java.util.Collection;

import org.eobjects.analyzer.job.runner.FilterOutcomes;
import org.eobjects.analyzer.util.LabelUtils;

public class SimpleComponentRequirement implements ComponentRequirement {

    private static final long serialVersionUID = 1L;

    private final FilterOutcome _outcome;

    public SimpleComponentRequirement(FilterOutcome outcome) {
        if (outcome == null) {
            throw new IllegalArgumentException("FilterOutcome cannot be null");
        }
        _outcome = outcome;
    }
    
    /**
     * Gets the outcome that this {@link ComponentRequirement} represents
     * @return
     */
    public FilterOutcome getOutcome() {
        return _outcome;
    }
    
    @Override
    public Collection<FilterOutcome> getProcessingDependencies() {
        return Arrays.asList(_outcome);
    }

    @Override
    public boolean isSatisfied(FilterOutcomes outcomes) {
        return outcomes.contains(_outcome);
    }

    @Override
    public String toString() {
        final String filterLabel = LabelUtils.getLabel(_outcome.getFilterJob());
        return filterLabel + "=" + _outcome.getCategory();
    }
}
