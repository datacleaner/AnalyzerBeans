package org.eobjects.analyzer.beans;

import java.util.Map;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.annotations.Result;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

@AnalyzerBean
public class ValueDistributionAnalyzer implements RowProcessingAnalyzer {

	@Configured
	Column column;

	@Provided
	Map<String, Long> valueDistribution;

	private long nullCount;
	private long uniqueCount;

	@Override
	public void run(Row row, long distinctCount) {
		Object value = row.getValue(column);
		runInternal(value, distinctCount);
	}

	public void runInternal(Object value, long distinctCount) {
		if (value == null) {
			nullCount += distinctCount;
		} else {
			String stringValue = value.toString();
			Long count = valueDistribution.get(stringValue);
			if (count == null) {
				count = distinctCount;
			} else {
				if (count == 1) {
					uniqueCount--;
				}
				count += distinctCount;
			}
			if (count == 1) {
				uniqueCount++;
			}
			valueDistribution.put(stringValue, count);
		}
	}

	@Result
	public long getNullCount() {
		return nullCount;
	}

	@Result
	public long getUniqueCount() {
		return uniqueCount;
	}

	@Result
	public Map<String, Long> getValueDistribution() {
		return valueDistribution;
	}
	
	public void setValueDistribution(Map<String, Long> valueDistribution) {
		this.valueDistribution = valueDistribution;
	}
}
