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
package org.eobjects.analyzer.result;

import java.io.Serializable;
import java.util.List;

import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.util.NullTolerableComparator;
import org.eobjects.metamodel.util.BaseObject;

/**
 * Represents a simple value and count pair, used by the
 * {@link ValueDistributionAnalyzer} to represent which values occur at what
 * frequencies.
 */
public final class ValueCount extends BaseObject implements Serializable, Comparable<ValueCount> {

    private static final long serialVersionUID = 1L;

    private final String _value;
    private final int _count;

    public ValueCount(String value, int count) {
        _value = value;
        _count = count;
    }

    public String getValue() {
        return _value;
    }

    public int getCount() {
        return _count;
    }

    @Override
    public String toString() {
        return "[" + _value + "->" + _count + "]";
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        identifiers.add(_value);
        identifiers.add(_count);
    }

    @Override
    public int compareTo(ValueCount o) {
        int diff = o.getCount() - getCount();
        if (diff == 0) {
            diff = NullTolerableComparator.get(String.class).compare(getValue(), o.getValue());
        }
        return diff;
    }
}
