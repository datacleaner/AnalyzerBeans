package org.eobjects.analyzer.beans.valuedist;

import java.util.Collection;
import java.util.Collections;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnalyzerResult;

public class ValueDistributionResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private ValueCountList _topValues;
	private ValueCountList _bottomValues;
	private int _nullCount;
	private Collection<String> _uniqueValues;
	private int _uniqueValueCount;
	private String _columnName;

	private ValueDistributionResult(InputColumn<?> column,
			ValueCountList topValues, ValueCountList bottomValues, int nullCount) {
		_columnName = column.getName();
		_topValues = topValues;
		_bottomValues = bottomValues;
		_nullCount = nullCount;
	}

	public ValueDistributionResult(InputColumn<?> column,
			ValueCountList topValues, ValueCountListImpl bottomValues,
			int nullCount, Collection<String> uniqueValues) {
		this(column, topValues, bottomValues, nullCount);
		_uniqueValues = uniqueValues;
	}

	public ValueDistributionResult(InputColumn<?> column,
			ValueCountList topValues, ValueCountListImpl bottomValues,
			int nullCount, int uniqueValueCount) {
		this(column, topValues, bottomValues, nullCount);
		_uniqueValueCount = uniqueValueCount;
	}

	@Override
	public Class<ValueDistributionAnalyzer> getProducerClass() {
		return ValueDistributionAnalyzer.class;
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
}
