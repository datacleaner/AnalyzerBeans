package org.eobjects.analyzer.result;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eobjects.analyzer.util.TimeInterval;

public class DateGapAnalyzerResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private Map<String, SortedSet<TimeInterval>> _gaps;
	private Map<String, SortedSet<TimeInterval>> _overlaps;

	public DateGapAnalyzerResult(Map<String, SortedSet<TimeInterval>> gaps, Map<String, SortedSet<TimeInterval>> overlaps) {
		_gaps = gaps;
		_overlaps = overlaps;
	}

	public Set<String> getGroupNames() {
		return _gaps.keySet();
	}

	public Map<String, SortedSet<TimeInterval>> getGaps() {
		return _gaps;
	}

	public Map<String, SortedSet<TimeInterval>> getOverlaps() {
		return _overlaps;
	}
}
