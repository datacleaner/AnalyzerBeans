package org.eobjects.analyzer.util;

import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

public class TimeIntervalTest extends TestCase {

	public void testCompareTo() throws Exception {
		TimeInterval ti1 = new TimeInterval(null, 100l);
		TimeInterval ti2 = new TimeInterval(100l, 200l);
		TimeInterval ti3 = new TimeInterval(100l, 300l);
		TimeInterval ti4 = new TimeInterval(200l, 300l);
		TimeInterval ti5 = new TimeInterval(null, 300l);
		TimeInterval ti6 = new TimeInterval(100l, null);

		assertTrue(ti2.before(ti4));
		assertTrue(ti1.before(ti4));
		assertTrue(ti1.before(ti5));
		assertTrue(ti5.after(ti1));

		assertTrue(ti3.before(ti6));

		SortedSet<TimeInterval> sortedSet = new TreeSet<TimeInterval>();
		sortedSet.add(ti1);
		sortedSet.add(ti2);
		sortedSet.add(ti3);
		sortedSet.add(ti4);
		sortedSet.add(ti5);
		sortedSet.add(ti6);

		assertEquals(
				"[TimeInterval[null->100], TimeInterval[null->300], TimeInterval[100->200], TimeInterval[100->300], TimeInterval[100->null], TimeInterval[200->300]]",
				sortedSet.toString());
	}
}
