package org.eobjects.analyzer.beans.coalesce;

import java.util.Date;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import dk.eobjects.metamodel.util.DateUtils;
import dk.eobjects.metamodel.util.Month;

import junit.framework.TestCase;

public class CoalesceDatesTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		MockInputColumn<Date> col1 = new MockInputColumn<Date>("col1", Date.class);
		MockInputColumn<Date> col2 = new MockInputColumn<Date>("col2", Date.class);
		MockInputColumn<Date> col3 = new MockInputColumn<Date>("col3", Date.class);

		@SuppressWarnings("unchecked")
		CoalesceDatesTransformer t = new CoalesceDatesTransformer(col1, col2, col3);
		assertEquals(1, t.getOutputColumns().getColumnCount());

		Date april1 = DateUtils.get(2000, Month.APRIL, 1);
		Date may1 = DateUtils.get(2000, Month.MAY, 1);
		Date june1 = DateUtils.get(2000, Month.JUNE, 1);
		assertEquals(april1, t.transform(new MockInputRow().put(col2, april1).put(col3, may1))[0]);
		assertEquals(may1, t.transform(new MockInputRow().put(col2, april1).put(col1, may1))[0]);
		assertEquals(may1, t.transform(new MockInputRow().put(col2, june1).put(col1, may1))[0]);

		assertNull(t.transform(new MockInputRow().put(col2, null).put(col1, null))[0]);
	}
}
