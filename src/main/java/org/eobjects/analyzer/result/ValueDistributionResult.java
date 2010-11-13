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
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.valuedist.ValueCount;
import org.eobjects.analyzer.beans.valuedist.ValueCountList;
import org.eobjects.analyzer.beans.valuedist.ValueCountListImpl;
import org.eobjects.analyzer.data.InputColumn;

public class ValueDistributionResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private ValueCountList _topValues;
	private ValueCountList _bottomValues;
	private int _nullCount;
	private Collection<String> _uniqueValues;
	private int _uniqueValueCount;
	private String _columnName;

	private ValueDistributionResult(InputColumn<?> column, ValueCountList topValues, ValueCountList bottomValues,
			int nullCount) {
		_columnName = column.getName();
		_topValues = topValues;
		_bottomValues = bottomValues;
		_nullCount = nullCount;
	}

	public ValueDistributionResult(InputColumn<?> column, ValueCountList topValues, ValueCountListImpl bottomValues,
			int nullCount, Collection<String> uniqueValues) {
		this(column, topValues, bottomValues, nullCount);
		_uniqueValues = uniqueValues;
	}

	public ValueDistributionResult(InputColumn<?> column, ValueCountList topValues, ValueCountListImpl bottomValues,
			int nullCount, int uniqueValueCount) {
		this(column, topValues, bottomValues, nullCount);
		_uniqueValueCount = uniqueValueCount;
	}

	public ValueCountList getTopValues() {
		return _topValues;
	}

	public ValueCountList getBottomValues() {
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
		return Collections.unmodifiableCollection(_uniqueValues);
	}

	public String getColumnName() {
		return _columnName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Value distribution for column: ");
		sb.append(_columnName);

		if (_topValues != null && _topValues.getActualSize() > 0) {
			sb.append("\nTop values:");
			List<ValueCount> valueCounts = _topValues.getValueCounts();
			for (ValueCount valueCount : valueCounts) {
				sb.append("\n - ");
				sb.append(valueCount.getValue());
				sb.append(": ");
				sb.append(valueCount.getCount());
			}
		}

		if (_bottomValues != null && _bottomValues.getActualSize() > 0) {
			sb.append("\nBottom values:");
			List<ValueCount> valueCounts = _bottomValues.getValueCounts();
			for (ValueCount valueCount : valueCounts) {
				sb.append("\n - ");
				sb.append(valueCount.getValue());
				sb.append(": ");
				sb.append(valueCount.getCount());
			}
		}

		sb.append("\nNull count: ");
		sb.append(_nullCount);

		sb.append("\nUnique values: ");
		if (_uniqueValues == null) {
			sb.append(_uniqueValueCount);
		} else {
			for (String value : _uniqueValues) {
				sb.append("\n - ");
				sb.append(value);
			}
		}
		return sb.toString();
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
}
