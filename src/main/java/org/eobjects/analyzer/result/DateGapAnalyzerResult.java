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

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eobjects.analyzer.util.TimeInterval;

public class DateGapAnalyzerResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private final Map<String, SortedSet<TimeInterval>> _gaps;
	private final Map<String, SortedSet<TimeInterval>> _overlaps;

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
