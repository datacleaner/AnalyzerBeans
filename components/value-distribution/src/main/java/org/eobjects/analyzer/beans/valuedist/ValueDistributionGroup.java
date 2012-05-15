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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.CollectionFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a value distribution within a {@link ValueDistributionAnalyzer}. A
 * {@link ValueDistributionGroup} contains the counted values within a single
 * group.
 * 
 * @author Kasper Sørensen
 */
class ValueDistributionGroup {

	private static final Logger logger = LoggerFactory
			.getLogger(ValueDistributionGroup.class);

	private final Map<String, Integer> _counterMap;
	private final Map<String, RowAnnotation> _annotationMap;
	private final RowAnnotationFactory _annotationFactory;
	private final String _groupName;
	private final boolean _recordAnnotations;
	private final InputColumn<?>[] _inputColumns;
	private int _nullCount;
	private int _totalCount;

	public ValueDistributionGroup(String groupName,
			CollectionFactory collectionFactory,
			RowAnnotationFactory annotationFactory, boolean recordAnnotations,
			InputColumn<?>[] inputColumns) {
		_groupName = groupName;
		_annotationFactory = annotationFactory;
		_recordAnnotations = recordAnnotations;
		_inputColumns = inputColumns;
		if (recordAnnotations) {
			_annotationMap = new HashMap<String, RowAnnotation>();
			_counterMap = null;
		} else {
			_annotationMap = null;
			_counterMap = collectionFactory.createMap(String.class,
					Integer.class);
		}
	}

	public synchronized void run(InputRow row, String value, int distinctCount) {
		if (value == null) {
			_nullCount += distinctCount;
		} else if (_recordAnnotations) {
			RowAnnotation annotation = _annotationMap.get(value);
			if (annotation == null) {
				annotation = _annotationFactory.createAnnotation();
				_annotationMap.put(value, annotation);
			}
			_annotationFactory.annotate(row, distinctCount, annotation);

		} else {
			Integer count = _counterMap.get(value);
			if (count == null) {
				count = 0;
			}
			count = count + distinctCount;
			_counterMap.put(value, count);
		}
		_totalCount += distinctCount;
	}

	public ValueDistributionGroupResult createResult(Integer topFrequentValues,
			Integer bottomFrequentValues, boolean recordUniqueValues) {
		final ValueCountListImpl topValues;
		final ValueCountListImpl bottomValues;
		if (topFrequentValues == null || bottomFrequentValues == null) {
			topValues = ValueCountListImpl.createFullList();
			bottomValues = null;
		} else {
			topValues = ValueCountListImpl.createTopList(topFrequentValues);
			bottomValues = ValueCountListImpl
					.createBottomList(bottomFrequentValues);
		}

		final List<String> uniqueValues;
		if (recordUniqueValues) {
			uniqueValues = new ArrayList<String>();
		} else {
			uniqueValues = null;
		}

		int uniqueCount = 0;
		final int entryCount;

		if (_recordAnnotations) {
			entryCount = _annotationMap.size();
			final Set<Entry<String, RowAnnotation>> entrySet = _annotationMap
					.entrySet();

			int i = 0;
			for (Entry<String, RowAnnotation> entry : entrySet) {
				if (i % 100000 == 0 && i != 0) {
					logger.info("Processing unique value entry no. {}", i);
				}
				final String value = entry.getKey();
				final RowAnnotation annotation = entry.getValue();
				final int count = annotation.getRowCount();
				uniqueCount = countValue(recordUniqueValues, topValues,
						bottomValues, uniqueValues, uniqueCount, value, count);
				i++;
			}
		} else {
			entryCount = _counterMap.size();
			final Set<Entry<String, Integer>> entrySet = _counterMap.entrySet();
			int i = 0;
			for (Entry<String, Integer> entry : entrySet) {
				if (i % 100000 == 0 && i != 0) {
					logger.info("Processing unique value entry no. {}", i);
				}
				final String value = entry.getKey();
				final Integer count = entry.getValue();
				uniqueCount = countValue(recordUniqueValues, topValues,
						bottomValues, uniqueValues, uniqueCount, value, count);
				i++;
			}
		}

		final int distinctCount;
		if (_nullCount > 0) {
			distinctCount = 1 + entryCount;
		} else {
			distinctCount = entryCount;
		}

		final Map<String, RowAnnotation> annotations;
		if (_recordAnnotations) {
			annotations = _annotationMap;
		} else {
			annotations = null;
		}

		if (recordUniqueValues) {
			return new ValueDistributionGroupResult(_groupName, topValues,
					bottomValues, _nullCount, uniqueValues, uniqueCount,
					distinctCount, _totalCount, annotations,
					_annotationFactory, _inputColumns);
		} else {
			return new ValueDistributionGroupResult(_groupName, topValues,
					bottomValues, _nullCount, uniqueCount, distinctCount,
					_totalCount, annotations, _annotationFactory, _inputColumns);
		}
	}

	private int countValue(boolean recordUniqueValues,
			ValueCountListImpl topValues, ValueCountListImpl bottomValues,
			final List<String> uniqueValues, int uniqueCount,
			final String value, final int count) {
		if (count == 1) {
			if (recordUniqueValues) {
				uniqueValues.add(value);
			}
			uniqueCount++;
		} else {
			ValueCount vc = new ValueCount(value, count);
			topValues.register(vc);
			if (bottomValues != null) {
				bottomValues.register(vc);
			}
		}
		return uniqueCount;
	}
}
