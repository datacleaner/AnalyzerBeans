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

import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;

/**
 * An abstract implementation of {@link ValueCountingAnalyzerResult} which
 * implements the most important metric: The value count.
 */
public abstract class AbstractValueCountingAnalyzerResult implements ValueCountingAnalyzerResult {

    private static final long serialVersionUID = 1L;

    @Metric(value = "Value count", supportsInClause = true)
    public final QueryParameterizableMetric getValueCount() {
        return new QueryParameterizableMetric() {

            @Override
            public Collection<String> getParameterSuggestions() {
                final Collection<ValueCount> valueCounts = AbstractValueCountingAnalyzerResult.this.getValueCounts();
                final List<String> result = CollectionUtils.map(valueCounts, new Func<ValueCount, String>() {
                    @Override
                    public String eval(ValueCount vc) {
                        return vc.getValue();
                    }
                });
                result.remove(null);
                result.remove(LabelUtils.NULL_LABEL);
                result.remove(LabelUtils.UNEXPECTED_LABEL);
                return result;
            }

            @Override
            public int getTotalCount() {
                return AbstractValueCountingAnalyzerResult.this.getTotalCount();
            }

            @Override
            public int getInstanceCount(String instance) {
                Integer count = getCount(instance);
                if (count == null) {
                    return 0;
                }
                return count;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Value distribution for: ");
        sb.append(getName());
        appendToString(sb, this, 4);
        return sb.toString();
    }

    /**
     * Appends a string representation with a maximum amount of entries
     * 
     * @param sb
     *            the StringBuilder to append to
     * 
     * @param maxEntries
     * @return
     */
    protected void appendToString(StringBuilder sb, ValueCountingAnalyzerResult groupResult, int maxEntries) {
        if (maxEntries != 0) {
            Collection<ValueCount> valueCounts = groupResult.getValueCounts();
            for (ValueCount valueCount : valueCounts) {
                sb.append("\n - ");
                sb.append(valueCount.getValue());
                sb.append(": ");
                sb.append(valueCount.getCount());

                maxEntries--;
                if (maxEntries == 0) {
                    sb.append("\n ...");
                    break;
                }
            }
        }

        sb.append("\nNull count: ");
        sb.append(groupResult.getNullCount());

        sb.append("\nUnique values: ");
        Collection<String> uniqueValues = groupResult.getUniqueValues();

        if (uniqueValues == null) {
            sb.append(groupResult.getUniqueCount());
        } else if (uniqueValues.isEmpty()) {
            sb.append("0");
        } else {
            for (String value : uniqueValues) {
                sb.append("\n - ");
                sb.append(value);

                maxEntries--;
                if (maxEntries == 0) {
                    sb.append("\n ...");
                    break;
                }
            }
        }
    }
}
