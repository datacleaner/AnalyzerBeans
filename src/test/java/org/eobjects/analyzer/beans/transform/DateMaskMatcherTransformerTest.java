package org.eobjects.analyzer.beans.transform;

import java.util.Arrays;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class DateMaskMatcherTransformerTest extends TestCase {

	public void testSimpleScenario() throws Exception {
		MockInputColumn<String> col = new MockInputColumn<String>("foo", String.class);
		DateMaskMatcherTransformer t = new DateMaskMatcherTransformer(col);
		
		t.setDateMasks(new String[] { "yyyy-MM-dd", "yyyy-dd-MM" });
		t.init();

		OutputColumns outputColumns = t.getOutputColumns();
		assertEquals(2, outputColumns.getColumnCount());
		assertEquals("foo 'yyyy-MM-dd'", outputColumns.getColumnName(0));
		assertEquals("foo 'yyyy-dd-MM'", outputColumns.getColumnName(1));

		assertEquals("[true, false]", Arrays.toString(t.transform(new MockInputRow().put(col, "2010-03-21"))));
		assertEquals("[false, false]", Arrays.toString(t.transform(new MockInputRow().put(col, "hello world"))));
		assertEquals("[false, false]", Arrays.toString(t.transform(new MockInputRow().put(col, null))));
		assertEquals("[true, true]", Arrays.toString(t.transform(new MockInputRow().put(col, "2010-03-11"))));
		assertEquals("[false, false]", Arrays.toString(t.transform(new MockInputRow().put(col, "2010/03/21"))));
	}
}
