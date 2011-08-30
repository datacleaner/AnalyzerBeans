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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.result.ValueDistributionGroupResult;
import org.eobjects.analyzer.storage.CollectionFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(ValueDistributionGroup.class);

	private final Map<String, Integer> _map;
	private final String _groupName;
	private int _nullCount;
	private int _totalCount;

	public ValueDistributionGroup(String groupName, CollectionFactory collectionFactory) {
		_groupName = groupName;
		_map = collectionFactory.createMap(String.class, Integer.class);
	}

	public synchronized void run(String value, int distinctCount) {
		if (value == null) {
			_nullCount += distinctCount;
		} else {
			Integer count = _map.get(value);
			if (count == null) {
				count = 0;
			}
			count = count + distinctCount;
			_map.put(value, count);
		}
		_totalCount += distinctCount;
	}

	public ValueDistributionGroupResult createResult(Integer topFrequentValues, Integer bottomFrequentValues,
			boolean recordUniqueValues) {
		ValueCountListImpl topValues;
		ValueCountListImpl bottomValues;
		if (topFrequentValues == null || bottomFrequentValues == null) {
			topValues = ValueCountListImpl.createFullList();
			bottomValues = null;
		} else {
			topValues = ValueCountListImpl.createTopList(topFrequentValues);
			bottomValues = ValueCountListImpl.createBottomList(bottomFrequentValues);
		}

		final List<String> uniqueValues;
		if (recordUniqueValues) {
			uniqueValues = new ArrayList<String>();
		} else {
			uniqueValues = null;
		}
		int uniqueCount = 0;
		final Set<Entry<String, Integer>> entrySet = _map.entrySet();
		int entryCount = 0;
		for (Entry<String, Integer> entry : entrySet) {
			if (entryCount % 100000 == 0 && entryCount != 0) {
				logger.info("Processing unique value entry no. {}", entryCount);
			}
			if (entry.getValue() == 1) {
				if (recordUniqueValues) {
					uniqueValues.add(entry.getKey());
				}
				uniqueCount++;
			} else {
				ValueCount vc = new ValueCount(entry.getKey(), entry.getValue());
				topValues.register(vc);
				if (bottomValues != null) {
					bottomValues.register(vc);
				}
			}
			entryCount++;
		}

		int distinctCount;
		if (_nullCount > 0) {
			distinctCount = 1 + entryCount;
		} else {
			distinctCount = entryCount;
		}

		if (recordUniqueValues) {
			return new ValueDistributionGroupResult(_groupName, topValues, bottomValues, _nullCount, uniqueValues,
					uniqueCount, distinctCount, _totalCount);
		} else {
			return new ValueDistributionGroupResult(_groupName, topValues, bottomValues, _nullCount, uniqueCount,
					distinctCount, _totalCount);
		}
	}
}
