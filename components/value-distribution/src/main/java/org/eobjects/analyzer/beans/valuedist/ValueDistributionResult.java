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
package org.eobjects.analyzer.beans.valuedist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.analyzer.result.QueryParameterizableMetric;

/**
 * Represents the result of the {@link ValueDistributionAnalyzer}.
 * 
 * A value distribution result has two basic forms: Grouped or ungrouped. To
 * find out which type a particular instance has, use the
 * {@link #isGroupingEnabled()} method.
 * 
 * Ungrouped results only contain a single/global value distribution. A grouped
 * result contain multiple value distributions, based on groups.
 * 
 * @author Kasper Sørensen
 */
public class ValueDistributionResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<?> _column;
    private final InputColumn<String> _groupColumn;
    private final SortedSet<ValueDistributionGroupResult> _result;

    public ValueDistributionResult(InputColumn<?> column, ValueDistributionGroupResult ungroupedResult) {
        _column = column;
        _groupColumn = null;
        _result = new TreeSet<ValueDistributionGroupResult>();
        _result.add(ungroupedResult);
    }

    public ValueDistributionResult(InputColumn<?> column, InputColumn<String> groupColumn,
            SortedSet<ValueDistributionGroupResult> groupedResult) {
        _column = column;
        _groupColumn = groupColumn;
        _result = groupedResult;
    }

    public String getColumnName() {
        return _column.getName();
    }

    public String getGroupColumnName() {
        if (_groupColumn == null) {
            return null;
        }
        return _groupColumn.getName();
    }

    public boolean isGroupingEnabled() {
        return _groupColumn != null;
    }

    @Metric("Distinct count")
    public int getDistinctCount() {
        return getSingleValueDistributionResult().getDistinctCount();
    }

    @Metric("Null count")
    public int getNullCount() {
        return getSingleValueDistributionResult().getNullCount();
    }

    @Metric("Total count")
    public int getTotalCount() {
        return getSingleValueDistributionResult().getTotalCount();
    }

    @Metric(value = "Value count", supportsInClause = true)
    public QueryParameterizableMetric getValueCount() {
        return new QueryParameterizableMetric() {

            @Override
            public Collection<String> getParameterSuggestions() {
                return getValueCountSuggestions();
            }

            @Override
            public int getTotalCount() {
                return ValueDistributionResult.this.getTotalCount();
            }

            @Override
            public int getInstanceCount(String instance) {
                Integer count = getSingleValueDistributionResult().getCount(instance);
                if (count == null) {
                    return 0;
                }
                return count;
            }
        };
    }

    public Collection<String> getValueCountSuggestions() {
        List<ValueCount> valueCounts = getSingleValueDistributionResult().getTopValues().getValueCounts();
        List<String> suggestions = new ArrayList<String>();
        for (ValueCount valueCount : valueCounts) {
            suggestions.add(valueCount.getValue());
        }
        return suggestions;
    }

    @Metric("Unique count")
    public int getUniqueCount() {
        return getSingleValueDistributionResult().getUniqueCount();
    }

    public Set<ValueDistributionGroupResult> getGroupedValueDistributionResults() {
        if (!isGroupingEnabled()) {
            throw new IllegalStateException("This result is not a grouped result based Value Distribution result");
        }
        return Collections.unmodifiableSet(_result);
    }

    public ValueDistributionGroupResult getSingleValueDistributionResult() {
        if (isGroupingEnabled()) {
            throw new IllegalStateException("This result is not a single result based Value Distribution result");
        }
        return _result.iterator().next();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Value distribution for column: ");
        sb.append(_column.getName());
        if (isGroupingEnabled()) {
            for (ValueDistributionGroupResult valueDistributionGroupResult : getGroupedValueDistributionResults()) {
                sb.append("\n");
                sb.append("\nGroup: ");
                sb.append(valueDistributionGroupResult.getGroupName());

                valueDistributionGroupResult.appendToString(sb, 4);
            }
        } else {
            ValueDistributionGroupResult valueDistributionGroupResult = getSingleValueDistributionResult();
            valueDistributionGroupResult.appendToString(sb, 8);
        }
        return sb.toString();
    }
}
