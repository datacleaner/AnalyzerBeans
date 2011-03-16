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
package org.eobjects.analyzer.beans.transform;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.metamodel.util.DateUtils;
import org.eobjects.metamodel.util.Month;

import junit.framework.TestCase;

public class DatePartTransformerTest extends TestCase {

	public void testTransformDefaultDateConfiguration() throws Exception {
		DatePartTransformer transformer = new DatePartTransformer();
		MockInputColumn<Date> column = new MockInputColumn<Date>("my date", Date.class);
		transformer.column = column;

		OutputColumns outputColumns = transformer.getOutputColumns();
		assertEquals(3, outputColumns.getColumnCount());
		assertEquals("my date (year)", outputColumns.getColumnName(0));
		assertEquals("my date (month)", outputColumns.getColumnName(1));
		assertEquals("my date (day of month)", outputColumns.getColumnName(2));

		Date date = DateUtils.get(2011, Month.MARCH, 16);
		Number[] result = transformer.transform(new MockInputRow().put(column, date));
		assertEquals(3, result.length);
		assertEquals(2011, result[0]);
		assertEquals(3, result[1]);
		assertEquals(16, result[2]);
	}

	public void testNullDate() throws Exception {
		DatePartTransformer transformer = new DatePartTransformer();
		MockInputColumn<Date> column = new MockInputColumn<Date>("my date", Date.class);
		transformer.column = column;

		Number[] result = transformer.transform(new MockInputRow().put(column, null));
		assertEquals(3, result.length);
		assertEquals(null, result[0]);
		assertEquals(null, result[1]);
		assertEquals(null, result[2]);
	}

	public void testTransformTime() throws Exception {
		DatePartTransformer transformer = new DatePartTransformer();
		MockInputColumn<Date> column = new MockInputColumn<Date>("my time", Date.class);
		transformer.column = column;
		transformer.year = false;
		transformer.month = false;
		transformer.dayOfMonth = false;
		transformer.hour = true;
		transformer.minute = true;
		transformer.second = true;
		
		OutputColumns outputColumns = transformer.getOutputColumns();
		assertEquals(3, outputColumns.getColumnCount());
		assertEquals("my time (hour)", outputColumns.getColumnName(0));
		assertEquals("my time (minute)", outputColumns.getColumnName(1));
		assertEquals("my time (second)", outputColumns.getColumnName(2));

		Date date = new SimpleDateFormat("HH:mm:ss").parse("13:21:55");
		Number[] result = transformer.transform(date);
		assertEquals(3, result.length);
		assertEquals(13, result[0]);
		assertEquals(21, result[1]);
		assertEquals(55, result[2]);
	}
}
