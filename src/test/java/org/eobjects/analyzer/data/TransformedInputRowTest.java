package org.eobjects.analyzer.data;

import dk.eobjects.metamodel.schema.Column;
import junit.framework.TestCase;

public class TransformedInputRowTest extends TestCase {

	public void testGetValue() throws Exception {
		Column col1 = new Column("foo");
		Column col2 = new Column("bar");
		InputColumn<?> inputColumn1 = new MetaModelInputColumn(col1);
		InputColumn<?> inputColumn2 = new MetaModelInputColumn(col2);
		InputColumn<String> inputColumn3 = new VirtualStringInputColumn("w00p");
		
		TransformedInputRow row1 = new TransformedInputRow(null);
		row1.addValue(inputColumn1, "f");
		row1.addValue(inputColumn2, "b");
		assertEquals("f", row1.getValue(inputColumn1));
		assertEquals("b", row1.getValue(inputColumn2));
		assertNull(row1.getValue(inputColumn3));
		assertNull(row1.getValue(null));
		
		TransformedInputRow row2 = new TransformedInputRow(row1);
		assertEquals("f", row2.getValue(inputColumn1));
		assertEquals("b", row2.getValue(inputColumn2));
		
		row2.addValue(inputColumn3, "w");
		
		assertNull(row1.getValue(inputColumn3));
		assertEquals("w", row2.getValue(inputColumn3));
	}
}
