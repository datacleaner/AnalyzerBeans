package org.eobjects.analyzer.beans.valuedist;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

@AnalyzerBean("Value distribution")
public class ValueDistributionAnalyzer implements RowProcessingAnalyzer {

	private static final Logger logger = LoggerFactory
			.getLogger(ValueDistributionAnalyzer.class);

	@Inject
	@Configured("Column")
	Column _column;

	@Inject
	@Configured("Record unique values")
	boolean _recordUniqueValues;

	@Inject
	@Configured("Top n most frequent values")
	Integer _topFrequentValues;

	@Inject
	@Configured("Bottom n most frequent values")
	Integer _bottomFrequentValues;

	@Inject
	@Provided
	Map<String, Integer> _valueDistribution;

	private int _nullCount;

	public ValueDistributionAnalyzer(Column column, boolean recordUniqueValues,
			Integer topFrequentValues, Integer bottomFrequentValues) {
		_column = column;
		_recordUniqueValues = recordUniqueValues;
		_topFrequentValues = topFrequentValues;
		_bottomFrequentValues = bottomFrequentValues;
	}
	
	public ValueDistributionAnalyzer() {
	}

	@Override
	public void run(Row row, int distinctCount) {
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

	@Result
	public ValueDistributionResult getResult() {
		logger.info("getResult()");
		ValueCountListImpl topValues;
		ValueCountListImpl bottomValues;
		if (_topFrequentValues == null && _bottomFrequentValues == null) {
			topValues = ValueCountListImpl.createFullList();
			bottomValues = null;
		} else {
			topValues = ValueCountListImpl.createTopList(_topFrequentValues);
			bottomValues = ValueCountListImpl
					.createBottomList(_bottomFrequentValues);
		}

		List<String> uniqueValues = new LinkedList<String>();
		int uniqueCount = 0;
		Set<Entry<String, Integer>> entrySet = _valueDistribution.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			if (entry.getValue() == 1) {
				if (_recordUniqueValues) {
					uniqueValues.add(entry.getKey());
				} else {
					uniqueCount++;
				}
			} else {
				ValueCount vc = new ValueCount(entry.getKey(), entry.getValue());
				topValues.register(vc);
				if (bottomValues != null) {
					bottomValues.register(vc);
				}
			}
		}

		if (_recordUniqueValues) {
			return new ValueDistributionResult(_column, topValues,
					bottomValues, _nullCount, uniqueValues);
		} else {
			return new ValueDistributionResult(_column, topValues,
					bottomValues, _nullCount, uniqueCount);
		}
	}

	public void setValueDistribution(Map<String, Integer> valueDistribution) {
		_valueDistribution = valueDistribution;
	}
}
