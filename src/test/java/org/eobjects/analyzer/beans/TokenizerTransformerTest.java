package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.TransformedInputRow;

import dk.eobjects.metamodel.schema.Column;

import junit.framework.TestCase;

public class TokenizerTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		InputColumn<?> col = new MetaModelInputColumn(new Column("name"));

		@SuppressWarnings("unchecked")
		TokenizerTransformer transformer = new TokenizerTransformer(
				(InputColumn<String>) col, 2);

		assertEquals(2, transformer.getOutputColumns().getColumnCount());

		TransformedInputRow row = new TransformedInputRow(null);
		row.addValue(col, "Kasper Sorensen");
		String[] values = transformer.transform(row);
		assertEquals(2, values.length);
		assertEquals("Kasper", values[0]);
		assertEquals("Sorensen", values[1]);

		row = new TransformedInputRow(null);
		row.addValue(col, "Kasper ");
		values = transformer.transform(row);
		assertEquals(2, values.length);
		assertEquals("Kasper", values[0]);
		assertNull(values[1]);
	}
}
