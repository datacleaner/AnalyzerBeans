package org.eobjects.analyzer.storage;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class InMemoryRowAnnotationFactoryTest extends TestCase {

	public void testGetValueCounts() throws Exception {
		InMemoryRowAnnotationFactory f = new InMemoryRowAnnotationFactory();
		RowAnnotation a = f.createAnnotation();

		MockInputColumn<String> col1 = new MockInputColumn<String>("greeting", String.class);
		MockInputColumn<String> col2 = new MockInputColumn<String>("greeter", String.class);

		f.annotate(new MockInputRow(1).put(col1, "hello").put(col2, "world"), 3, a);

		assertEquals(3, f.getValueCounts(a, col1).get("hello").intValue());
		assertEquals(3, f.getValueCounts(a, col2).get("world").intValue());
		
		f.annotate(new MockInputRow(2).put(col1, "hi").put(col2, "world"), 2, a);
		
		assertEquals(3, f.getValueCounts(a, col1).get("hello").intValue());
		assertEquals(2, f.getValueCounts(a, col1).get("hi").intValue());
		assertEquals(5, f.getValueCounts(a, col2).get("world").intValue());
	}
}
