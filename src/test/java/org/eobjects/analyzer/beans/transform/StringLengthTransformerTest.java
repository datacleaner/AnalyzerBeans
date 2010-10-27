package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class StringLengthTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		MockInputColumn<String> col = new MockInputColumn<String>("foobar", String.class);
		StringLengthTransformer transformer = new StringLengthTransformer(col);
		assertEquals(1, transformer.getOutputColumns().getColumnCount());
		Number[] result;
		
		result = transformer.transform(new MockInputRow().put(col, "hello"));
		assertEquals(1, result.length);
		assertEquals(5, result[0]);
		
		result = transformer.transform(new MockInputRow().put(col, null));
		assertEquals(1, result.length);
		assertNull(result[0]);
		
		result = transformer.transform(new MockInputRow().put(col, ""));
		assertEquals(1, result.length);
		assertEquals(0, result[0]);
	}
}
