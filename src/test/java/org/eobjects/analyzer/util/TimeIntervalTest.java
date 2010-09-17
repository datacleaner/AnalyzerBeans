package org.eobjects.analyzer.util;

import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

public class TimeIntervalTest extends TestCase {

	public void testCompareTo() throws Exception {
		TimeInterval ti1 = new TimeInterval(100l, 200l);
		TimeInterval ti2 = new TimeInterval(100l, 300l);
		TimeInterval ti3 = new TimeInterval(200l, 300l);

		assertTrue(ti1.before(ti2));
		assertTrue(ti1.before(ti3));
		assertTrue(ti2.before(ti3));

		SortedSet<TimeInterval> sortedSet = new TreeSet<TimeInterval>();
		sortedSet.add(ti1);
		sortedSet.add(ti2);
		sortedSet.add(ti3);

		assertEquals(
				"[TimeInterval[100->200], TimeInterval[100->300], TimeInterval[200->300]]",
				sortedSet.toString());
	}

	public void testMerge() throws Exception {
		TimeInterval interval;

		interval = TimeInterval.merge(new TimeInterval(100l, 200l),
				new TimeInterval(150l, 250l));
		assertEquals(100l, interval.getFrom());
		assertEquals(250l, interval.getTo());

		interval = TimeInterval.merge(new TimeInterval(200l, 220l),
				new TimeInterval(150l, 250l));
		assertEquals(150l, interval.getFrom());
		assertEquals(250l, interval.getTo());

		interval = TimeInterval.merge(new TimeInterval(100l, 200l),
				new TimeInterval(220l, 250l));
		assertEquals(100l, interval.getFrom());
		assertEquals(250l, interval.getTo());
	}

	public void testOverlapsWith() throws Exception {
		TimeInterval ti1 = new TimeInterval(100l, 200l);
		assertTrue(ti1.overlapsWith(ti1));

		TimeInterval ti2 = new TimeInterval(150l, 250l);
		assertTrue(ti1.overlapsWith(ti2));
		assertTrue(ti2.overlapsWith(ti1));

		TimeInterval ti3 = new TimeInterval(250l, 300l);
		assertFalse(ti1.overlapsWith(ti3));
		assertFalse(ti3.overlapsWith(ti1));
		
		TimeInterval ti4 = new TimeInterval(100l, 150l);
		assertTrue(ti1.overlapsWith(ti4));
		assertTrue(ti4.overlapsWith(ti1));
	}
}
