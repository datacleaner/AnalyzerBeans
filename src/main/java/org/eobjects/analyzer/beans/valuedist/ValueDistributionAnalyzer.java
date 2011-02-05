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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Value distribution")
@Description("Gets the distributions of values that occur in a dataset.\nOften used as an initial way to see if a lot of repeated values are to be expected, if nulls occur and if a few un-repeated values add exceptions to the typical usage-pattern.")
public class ValueDistributionAnalyzer implements RowProcessingAnalyzer<ValueDistributionResult> {

	private static final Logger logger = LoggerFactory.getLogger(ValueDistributionAnalyzer.class);

	@Inject
	@Configured("Column")
	InputColumn<?> _column;

	@Inject
	@Configured("Record unique values")
	boolean _recordUniqueValues = true;

	@Inject
	@Configured(value = "Top n most frequent values", required = false)
	Integer _topFrequentValues;

	@Inject
	@Configured(value = "Bottom n most frequent values", required = false)
	Integer _bottomFrequentValues;

	@Inject
	@Provided
	Map<String, Integer> _valueDistribution;

	private int _nullCount;

	public ValueDistributionAnalyzer(InputColumn<?> column, boolean recordUniqueValues, Integer topFrequentValues,
			Integer bottomFrequentValues) {
		_column = column;
		_recordUniqueValues = recordUniqueValues;
		_topFrequentValues = topFrequentValues;
		_bottomFrequentValues = bottomFrequentValues;
	}

	public ValueDistributionAnalyzer() {
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		Object value = row.getValue(_column);
		runInternal(value, distinctCount);
	}

	public void runInternal(Object value, int distinctCount) {
		if (value == null) {
			logger.debug("value is null");
			_nullCount += distinctCount;
		} else {
			String stringValue = value.toString();
			Integer count = _valueDistribution.get(stringValue);
			if (count == null) {
				count = distinctCount;
			} else {
				count += distinctCount;
			}
			_valueDistribution.put(stringValue, count);
		}
	}

	@Override
	public ValueDistributionResult getResult() {
		logger.info("getResult()");
		ValueCountListImpl topValues;
		ValueCountListImpl bottomValues;
		if (_topFrequentValues == null || _bottomFrequentValues == null) {
			topValues = ValueCountListImpl.createFullList();
			bottomValues = null;
		} else {
			topValues = ValueCountListImpl.createTopList(_topFrequentValues);
			bottomValues = ValueCountListImpl.createBottomList(_bottomFrequentValues);
		}

		final Map<String, Integer> uniqueValues = CollectionUtils.createCacheMap();
		int uniqueCount = 0;
		final Set<Entry<String, Integer>> entrySet = _valueDistribution.entrySet();
		int entryCount = 0;
		for (Entry<String, Integer> entry : entrySet) {
			if (entryCount % 100000 == 0 && entryCount != 0) {
				logger.info("Processing unique value entry no. {}", entryCount);
			}
			if (entry.getValue() == 1) {
				if (_recordUniqueValues) {
					uniqueValues.put(entry.getKey(), Integer.valueOf(1));
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

		if (_recordUniqueValues) {
			return new ValueDistributionResult(_column, topValues, bottomValues, _nullCount, uniqueValues.keySet(),
					uniqueCount);
		} else {
			return new ValueDistributionResult(_column, topValues, bottomValues, _nullCount, uniqueCount);
		}
	}

	public void setValueDistribution(Map<String, Integer> valueDistribution) {
		_valueDistribution = valueDistribution;
	}

	public void setRecordUniqueValues(boolean recordUniqueValues) {
		_recordUniqueValues = recordUniqueValues;
	}
}
