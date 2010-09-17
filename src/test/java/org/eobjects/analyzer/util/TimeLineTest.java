package org.eobjects.analyzer.util;

import java.util.SortedSet;

import junit.framework.TestCase;

public class TimeLineTest extends TestCase {

	public void testGetFirstAndLast() throws Exception {
		TimeLine timeline = new TimeLine();

		timeline.addInterval(new TimeInterval(500l, 600l));
		timeline.addInterval(new TimeInterval(600l, 700l));
		timeline.addInterval(new TimeInterval(100l, 200l));
		timeline.addInterval(new TimeInterval(700l, 800l));
		timeline.addInterval(new TimeInterval(200l, 300l));

		assertEquals(100l, timeline.getFrom().getTime());
		assertEquals(800l, timeline.getTo().getTime());
	}

	public void testGetFlattenedIntervals() throws Exception {
		TimeLine timeline = new TimeLine();
		SortedSet<TimeInterval> result;

		result = timeline.getFlattenedIntervals();
		assertEquals("[]", result.toString());

		timeline.addInterval(new TimeInterval(500l, 600l));
		timeline.addInterval(new TimeInterval(600l, 700l));
		timeline.addInterval(new TimeInterval(100l, 200l));
		timeline.addInterval(new TimeInterval(700l, 800l));
		timeline.addInterval(new TimeInterval(200l, 300l));
		timeline.addInterval(new TimeInterval(250l, 280l));

		result = timeline.getFlattenedIntervals();
		assertEquals("[TimeInterval[100->300], TimeInterval[500->800]]",
				result.toString());
	}

	public void testGetOverlappingIntervals() throws Exception {
		TimeLine timeline = new TimeLine();
		timeline.addInterval(new TimeInterval(500l, 600l));
		timeline.addInterval(new TimeInterval(600l, 700l));
		assertEquals(0, timeline.getOverlappingIntervals(false).size());

		SortedSet<TimeInterval> overlappingIntervals = timeline
				.getOverlappingIntervals(true);
		assertEquals(1, overlappingIntervals.size());
		assertEquals("[TimeInterval[600->600]]",
				overlappingIntervals.toString());

		timeline.addInterval(new TimeInterval(600l, 650l));

		overlappingIntervals = timeline.getOverlappingIntervals(true);
		assertEquals(1, overlappingIntervals.size());
		assertEquals("[TimeInterval[600->650]]",
				overlappingIntervals.toString());

		timeline.addInterval(new TimeInterval(900l, 950l));
		timeline.addInterval(new TimeInterval(920l, 1000l));

		overlappingIntervals = timeline.getOverlappingIntervals(true);
		assertEquals(2, overlappingIntervals.size());
		assertEquals("[TimeInterval[600->650], TimeInterval[920->950]]",
				overlappingIntervals.toString());
	}
}
