package org.eobjects.analyzer.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.TimeInterval;

@AnalyzerBean("Date gap analyzer")
public class DateGapAnalyzer implements RowProcessingAnalyzer<AnalyzerResult> {

	@Configured
	InputColumn<Date> fromColumn;

	@Configured
	InputColumn<Date> toColumn;

	@Configured(required = false)
	InputColumn<String> groupColumn;

	Map<String, Set<TimeInterval>> timeIntervals;

	public DateGapAnalyzer() {
	}

	public DateGapAnalyzer(InputColumn<Date> fromColumn,
			InputColumn<Date> toColumn, InputColumn<String> groupColumn) {
		this.fromColumn = fromColumn;
		this.toColumn = toColumn;
		this.groupColumn = groupColumn;
		init();
	}

	@Initialize
	public void init() {
		timeIntervals = new HashMap<String, Set<TimeInterval>>();
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		Date from = row.getValue(fromColumn);
		Date to = row.getValue(toColumn);

		String groupName = "";
		if (groupColumn != null) {
			groupName = row.getValue(groupColumn);
		}

		put(groupName, new TimeInterval(from, to));
	}

	protected void put(String groupName, TimeInterval interval) {
		Set<TimeInterval> intervals = timeIntervals.get(groupName);
		if (intervals == null) {
			intervals = new TreeSet<TimeInterval>();
			timeIntervals.put(groupName, intervals);
		}
		intervals.add(interval);
	}

	@Override
	public AnalyzerResult getResult() {
		// TODO
		return null;
	}

}
