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
				new String[] { "first name", "last name" },
				(InputColumn<String>) col);

		InputColumn<String>[] virtualInputColumns = transformer
				.getVirtualInputColumns();
		assertEquals(2, virtualInputColumns.length);
		assertEquals("first name", virtualInputColumns[0].getName());
		assertEquals("last name", virtualInputColumns[1].getName());

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
