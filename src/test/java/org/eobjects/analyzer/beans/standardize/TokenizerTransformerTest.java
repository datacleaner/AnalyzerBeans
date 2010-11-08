package org.eobjects.analyzer.beans.standardize;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.standardize.TokenizerTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.TransformedInputRow;

import dk.eobjects.metamodel.schema.MutableColumn;

public class TokenizerTransformerTest extends TestCase {

	public void testGetOutputColumns() throws Exception {
		InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

		@SuppressWarnings("unchecked")
		TokenizerTransformer transformer = new TokenizerTransformer((InputColumn<String>) col, 2);

		OutputColumns oc = transformer.getOutputColumns();

		assertEquals(2, oc.getColumnCount());
		assertEquals("name (token 1)", oc.getColumnName(0));
		assertEquals("name (token 2)", oc.getColumnName(1));
	}

	public void testTransform() throws Exception {
		InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

		@SuppressWarnings("unchecked")
		TokenizerTransformer transformer = new TokenizerTransformer((InputColumn<String>) col, 2);

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
