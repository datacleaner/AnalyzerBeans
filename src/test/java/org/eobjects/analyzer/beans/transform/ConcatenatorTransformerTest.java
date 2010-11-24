package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class ConcatenatorTransformerTest extends TestCase {

	public void testConcat() throws Exception {
		InputColumn<String> col1 = new MockInputColumn<String>("str", String.class);
		InputColumn<Boolean> col2 = new MockInputColumn<Boolean>("bool", Boolean.class);

		ConcatenatorTransformer t = new ConcatenatorTransformer(" + ", new InputColumn[] { col1, col2 });
		
		assertEquals(1, t.getOutputColumns().getColumnCount());
		assertEquals("Concat of str,bool", t.getOutputColumns().getColumnName(0));

		String[] result = t.transform(new MockInputRow().put(col1, "hello").put(col2, true));
		assertEquals(1, result.length);
		assertEquals("hello + true", result[0]);
		
		result = t.transform(new MockInputRow().put(col1, "hi").put(col2, null));
		assertEquals(1, result.length);
		assertEquals("hi", result[0]);
		
		result = t.transform(new MockInputRow().put(col1, null).put(col2, true));
		assertEquals(1, result.length);
		assertEquals("true", result[0]);
		
		result = t.transform(new MockInputRow().put(col1, null).put(col2, null));
		assertEquals(1, result.length);
		assertEquals("", result[0]);
	}
}
