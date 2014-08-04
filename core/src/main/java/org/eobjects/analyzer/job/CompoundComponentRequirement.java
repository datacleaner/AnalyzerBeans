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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an outcome that is a product of multiple other outcomes and thus
 * requires just any of the provided outcomes.
 */
public class CompoundComponentRequirement implements ComponentRequirement {

    private static final long serialVersionUID = 1L;

    private final Set<Outcome> _outcomes;

    public CompoundComponentRequirement(Collection<? extends Outcome> outcomes) {
        _outcomes = new HashSet<Outcome>();
        for (Outcome outcome : outcomes) {
            _outcomes.add(outcome);
        }
    }

    public CompoundComponentRequirement(Outcome... outcomes) {
        _outcomes = new HashSet<Outcome>();
        for (Outcome outcome : outcomes) {
            _outcomes.add(outcome);
        }
    }

    @Override
    public boolean isSatisfied(Collection<Outcome> outcomes) {
        for (Outcome outcome : outcomes) {
            if (_outcomes.contains(outcome)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Outcome outcome : _outcomes) {
            if (sb.length() != 0) {
                sb.append(" OR ");
            }
            sb.append(outcome.toString());
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_outcomes == null) ? 0 : _outcomes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CompoundComponentRequirement other = (CompoundComponentRequirement) obj;
        if (_outcomes == null) {
            if (other._outcomes != null)
                return false;
        } else if (!_outcomes.equals(other._outcomes))
            return false;
        return true;
    }
}
