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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.job.Outcome;

/**
 * Default implementation of {@link OutcomeSink}
 */
public final class OutcomeSinkImpl implements OutcomeSink {

    private final List<Outcome> outcomes;

    @SuppressWarnings("unchecked")
    public OutcomeSinkImpl() {
        this(Collections.EMPTY_LIST);
    }

    public OutcomeSinkImpl(Collection<? extends Outcome> availableOutcomes) {
        // always take a copy of the collection argument
        outcomes = new ArrayList<Outcome>(availableOutcomes);
    }

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

    @Override
    public OutcomeSink clone() {
        return new OutcomeSinkImpl(outcomes);
    }
}
