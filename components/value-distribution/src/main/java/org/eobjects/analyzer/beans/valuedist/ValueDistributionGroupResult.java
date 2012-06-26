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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.NullTolerableComparator;
import org.eobjects.metamodel.util.Ref;
import org.eobjects.metamodel.util.SerializableRef;

public class ValueDistributionGroupResult implements Serializable, Comparable<ValueDistributionGroupResult> {

    private static final long serialVersionUID = 1L;

    private final ValueCountList _topValues;
    private final ValueCountList _bottomValues;
    private final int _nullCount;
    private final Collection<String> _uniqueValues;
    private final Map<String, RowAnnotation> _annotations;
    private final InputColumn<?>[] _highlightedColumns;
    private final int _uniqueValueCount;
    private final String _groupName;
    private final int _totalCount;
    private final int _distinctCount;

    private final Ref<RowAnnotationFactory> _annotationFactoryRef;

    public ValueDistributionGroupResult(String groupName, ValueCountList topValues, ValueCountList bottomValues,
            int nullCount, Collection<String> uniqueValues, int uniqueValueCount, int distinctCount, int totalCount,
            Map<String, RowAnnotation> annotations, RowAnnotationFactory annotationFactory,
            InputColumn<?>[] highlightedColumns) {
        _groupName = groupName;
        _topValues = topValues;
        _bottomValues = bottomValues;
        _nullCount = nullCount;
        _uniqueValues = uniqueValues;
        _uniqueValueCount = uniqueValueCount;
        _totalCount = totalCount;
        _distinctCount = distinctCount;
        _annotations = annotations;
        _annotationFactoryRef = new SerializableRef<RowAnnotationFactory>(annotationFactory);
        _highlightedColumns = highlightedColumns;
    }

    public ValueDistributionGroupResult(String groupName, ValueCountList topValues, ValueCountList bottomValues,
            int nullCount, int uniqueValueCount, int distinctCount, int totalCount,
            Map<String, RowAnnotation> annotations, RowAnnotationFactory annotationFactory,
            InputColumn<?>[] highlightedColumns) {
        this(groupName, topValues, bottomValues, nullCount, null, uniqueValueCount, distinctCount, totalCount,
                annotations, annotationFactory, highlightedColumns);
    }

    public boolean isAnnotationsEnabled() {
        return _annotations != null;
    }

    public boolean hasAnnotation(String value) {
        if (_annotations == null) {
            return false;
        }

        return _annotations.containsKey(value);
    }

    public AnnotatedRowsResult getAnnotatedRows(String value) {
        final RowAnnotationFactory annotationFactory = _annotationFactoryRef.get();
        if (_annotations == null || annotationFactory == null) {
            return null;
        }

        final RowAnnotation annotation = _annotations.get(value);
        if (annotation == null) {
            return null;
        }

        return new AnnotatedRowsResult(annotation, annotationFactory, _highlightedColumns);
    }

    public ValueCountList getTopValues() {
        if (_topValues == null) {
            return ValueCountListImpl.emptyList();
        }
        return _topValues;
    }

    public ValueCountList getBottomValues() {
        if (_bottomValues == null) {
            return ValueCountListImpl.emptyList();
        }
        return _bottomValues;
    }

    public int getNullCount() {
        return _nullCount;
    }

    public int getUniqueCount() {
        if (_uniqueValues != null) {
            return _uniqueValues.size();
        }
        return _uniqueValueCount;
    }

    public Collection<String> getUniqueValues() {
        if (_uniqueValues == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(_uniqueValues);
    }

    public String getGroupName() {
        return _groupName;
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
    public void appendToString(StringBuilder sb, int maxEntries) {
        if (maxEntries != 0) {
            if (_topValues != null && _topValues.getActualSize() > 0) {
                sb.append("\nTop values:");
                final List<ValueCount> valueCounts = _topValues.getValueCounts();
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
        }

        if (maxEntries != 0) {
            if (_bottomValues != null && _bottomValues.getActualSize() > 0) {
                sb.append("\nBottom values:");
                final List<ValueCount> valueCounts = _bottomValues.getValueCounts();
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
        }

        sb.append("\nNull count: ");
        sb.append(_nullCount);

        sb.append("\nUnique values: ");
        if (_uniqueValues == null) {
            sb.append(_uniqueValueCount);
        } else if (_uniqueValues.isEmpty()) {
            sb.append("0");
        } else {
            for (String value : _uniqueValues) {
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

    public Integer getCount(final String value) {
        if (value == null) {
            return _nullCount;
        }

        if (_topValues != null) {
            List<ValueCount> valueCounts = _topValues.getValueCounts();
            for (ValueCount valueCount : valueCounts) {
                if (value.equals(valueCount.getValue())) {
                    return valueCount.getCount();
                }
            }
        }

        if (_bottomValues != null) {
            List<ValueCount> valueCounts = _bottomValues.getValueCounts();
            for (ValueCount valueCount : valueCounts) {
                if (value.equals(valueCount.getValue())) {
                    return valueCount.getCount();
                }
            }
        }

        if (_uniqueValues != null) {
            if (_uniqueValues.contains(value)) {
                return 1;
            }
        }

        return null;
    }

    public int getDistinctCount() {
        return _distinctCount;
    }

    public int getTotalCount() {
        return _totalCount;
    }

    @Override
    public int hashCode() {
        if (_groupName == null) {
            return -1;
        }
        return _groupName.hashCode();
    }

    @Override
    public int compareTo(ValueDistributionGroupResult o) {
        return NullTolerableComparator.get(String.class).compare(getGroupName(), o.getGroupName());
    }
}
