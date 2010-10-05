package org.eobjects.analyzer.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.DateGapAnalyzerResult;
import org.eobjects.analyzer.util.TimeInterval;
import org.eobjects.analyzer.util.TimeLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Date gap analyzer")
public class DateGapAnalyzer implements
		RowProcessingAnalyzer<DateGapAnalyzerResult> {

	private static final Logger logger = LoggerFactory
			.getLogger(DateGapAnalyzer.class);

	@Configured
	InputColumn<Date> fromColumn;

	@Configured
	InputColumn<Date> toColumn;

	@Configured(required = false)
	InputColumn<String> groupColumn;

	@Configured(required = false, value = "Count intersecting from and to dates as overlaps")
	Boolean singleDateOverlaps = false;

	Map<String, TimeLine> timelines;

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
		timelines = new HashMap<String, TimeLine>();
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		Date from = row.getValue(fromColumn);
		Date to = row.getValue(toColumn);

		if (from != null && to != null) {
			String groupName = "";
			if (groupColumn != null) {
				groupName = row.getValue(groupColumn);
			}

			put(groupName, new TimeInterval(from, to));
		} else {
			logger.info("Encountered row where from column or to column was null, ignoring");
		}
	}

	protected void put(String groupName, TimeInterval interval) {
		TimeLine timeline = timelines.get(groupName);
		if (timeline == null) {
			timeline = new TimeLine();
			timelines.put(groupName, timeline);
		}
		timeline.addInterval(interval);
	}

	@Override
	public DateGapAnalyzerResult getResult() {
		boolean includeSingleTimeInstanceIntervals = false;
		if (singleDateOverlaps != null) {
			includeSingleTimeInstanceIntervals = singleDateOverlaps
					.booleanValue();
		}
		Map<String, SortedSet<TimeInterval>> gaps = new HashMap<String, SortedSet<TimeInterval>>();
		Map<String, SortedSet<TimeInterval>> overlaps = new HashMap<String, SortedSet<TimeInterval>>();
		Set<String> keySet = timelines.keySet();
		for (String name : keySet) {
			TimeLine timeline = timelines.get(name);
			SortedSet<TimeInterval> timelineGaps = timeline
					.getTimeGapIntervals();
			SortedSet<TimeInterval> timelineOverlaps = timeline
					.getOverlappingIntervals(includeSingleTimeInstanceIntervals);
			gaps.put(name, timelineGaps);
			overlaps.put(name, timelineOverlaps);
		}

		return new DateGapAnalyzerResult(getClass(), gaps, overlaps);
	}

}
