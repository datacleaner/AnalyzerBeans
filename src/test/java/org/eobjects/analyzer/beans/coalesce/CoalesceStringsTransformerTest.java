package org.eobjects.analyzer.beans.coalesce;

import junit.framework.TestCase;

import org.eobjects.analyzer.test.MockInputColumn;
import org.eobjects.analyzer.test.MockInputRow;

public class CoalesceStringsTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		MockInputColumn<String> col1 = new MockInputColumn<String>("col1", String.class);
		MockInputColumn<String> col2 = new MockInputColumn<String>("col2", String.class);
		MockInputColumn<String> col3 = new MockInputColumn<String>("col3", String.class);

		@SuppressWarnings("unchecked")
		CoalesceStringsTransformer t = new CoalesceStringsTransformer(col1, col2, col3);
		assertEquals(1, t.getOutputColumns().getColumnCount());

		assertEquals("hello", t.transform(new MockInputRow().put(col2, "hello").put(col3, "world"))[0]);
		assertEquals("world", t.transform(new MockInputRow().put(col2, "hello").put(col1, "world"))[0]);
		assertEquals("hello", t.transform(new MockInputRow().put(col1, "hello").put(col2, "world"))[0]);

		assertNull(t.transform(new MockInputRow().put(col2, null).put(col1, null))[0]);
	}
}
