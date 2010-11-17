package org.eobjects.analyzer.beans;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.joda.time.DateTime;

public class DateAndTimeAnalyzerColumnDelegateTest extends TestCase {

	public void testMaxAndMinValues() throws Exception {
		DateAndTimeAnalyzerColumnDelegate d = new DateAndTimeAnalyzerColumnDelegate(new InMemoryRowAnnotationFactory());

		assertNull(d.getMaxDate());
		assertNull(d.getMinDate());
		assertNull(d.getMaxTime());
		assertNull(d.getMinTime());

		d.run(new DateTime(2010, 3, 21, 10, 0, 0, 0).toDate(), new MockInputRow(), 1);

		assertNotNull(d.getMaxDate());
		assertNotNull(d.getMinDate());
		assertNotNull(d.getMaxTime());
		assertNotNull(d.getMinTime());

		assertEquals("2010-03-21", d.getMaxDate().toString());
		assertEquals("2010-03-21", d.getMinDate().toString());
		assertEquals("10:00:00.000", d.getMaxTime().toString());
		assertEquals("10:00:00.000", d.getMinTime().toString());

		d.run(new DateTime(2010, 4, 23, 10, 0, 0, 0).toDate(), new MockInputRow(), 1);

		assertEquals("2010-04-23", d.getMaxDate().toString());
		assertEquals("2010-03-21", d.getMinDate().toString());

		d.run(new DateTime(2010, 2, 23, 10, 0, 0, 0).toDate(), new MockInputRow(), 1);

		assertEquals("2010-04-23", d.getMaxDate().toString());
		assertEquals("2010-02-23", d.getMinDate().toString());

		d.run(new DateTime(2010, 3, 11, 10, 5, 0, 0).toDate(), new MockInputRow(), 1);

		assertEquals("10:05:00.000", d.getMaxTime().toString());
		assertEquals("10:00:00.000", d.getMinTime().toString());

		assertEquals(1, d.getMaxDateAnnotation().getRowCount());
		assertEquals(1, d.getMinDateAnnotation().getRowCount());
		assertEquals(1, d.getMaxTimeAnnotation().getRowCount());
		assertEquals(3, d.getMinTimeAnnotation().getRowCount());

		assertEquals(4, d.getNumRows());
		assertEquals(0, d.getNumNull());
	}
}
