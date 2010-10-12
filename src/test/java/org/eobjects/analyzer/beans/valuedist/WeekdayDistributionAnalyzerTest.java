package org.eobjects.analyzer.beans.valuedist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.test.MockInputColumn;
import org.eobjects.analyzer.test.MockInputRow;

import junit.framework.TestCase;

public class WeekdayDistributionAnalyzerTest extends TestCase {

	public void testTypicalUsage() throws Exception {
		WeekdayDistributionAnalyzer analyzer = new WeekdayDistributionAnalyzer();

		@SuppressWarnings("unchecked")
		InputColumn<Date>[] dateColumns = new InputColumn[3];
		dateColumns[0] = new MockInputColumn<Date>("Order date", Date.class);
		dateColumns[1] = new MockInputColumn<Date>("Shipment date", Date.class);
		dateColumns[2] = new MockInputColumn<Date>("Delivery date", Date.class);

		analyzer.setDateColumns(dateColumns);
		analyzer.init();

		// 1x: friday, saturday, tuesday
		analyzer.run(new MockInputRow(dateColumns, new Object[] { d(2010, 1, 1), d(2010, 1, 2), d(2010, 1, 5) }), 1);
		// 2x: monday, tuesday, friday
		analyzer.run(new MockInputRow(dateColumns, new Object[] { d(2010, 2, 1), d(2010, 2, 2), d(2010, 2, 5) }), 2);
		// 1x: thursday, friday, monday
		analyzer.run(new MockInputRow(dateColumns, new Object[] { d(2010, 4, 1), d(2010, 4, 2), d(2010, 4, 5) }), 1);

		CrosstabResult result = analyzer.getResult();

		String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");
		assertEquals(8, resultLines.length);
		assertEquals("             Order date Shipment date Delivery date ", resultLines[0]);
		assertEquals("Sunday                0             0             0 ", resultLines[1]);
		assertEquals("Monday                2             0             1 ", resultLines[2]);
		assertEquals("Tuesday               0             2             1 ", resultLines[3]);
		assertEquals("Wednesday             0             0             0 ", resultLines[4]);
		assertEquals("Thursday              1             0             0 ", resultLines[5]);
		assertEquals("Friday                1             1             2 ", resultLines[6]);
		assertEquals("Saturday              0             1             0 ", resultLines[7]);
	}

	public void testDateGen() throws Exception {
		Date d = d(2010, 1, 1);
		assertEquals("2010-01-01", new SimpleDateFormat("yyyy-MM-dd").format(d));
	}

	private Date d(int year, int month, int date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0l);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DAY_OF_MONTH, date);
		return c.getTime();
	}

}
