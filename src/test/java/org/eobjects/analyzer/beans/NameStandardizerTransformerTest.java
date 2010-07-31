package org.eobjects.analyzer.beans;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MetaModelInputColumn;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class NameStandardizerTransformerTest extends TestCase {

	public void testSimpleScenario() throws Exception {
		NameStandardizerTransformer transformer = new NameStandardizerTransformer();
		Column column = new Column("name", ColumnType.VARCHAR);

		MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
		transformer.setInputColumn(inputColumn);

		transformer.init();

		assertEquals("[John, Doh, Doe]",
				Arrays.toString(transformer.transform("John Doe Doh")));

		assertEquals("[John, Doh, Doe]",
				Arrays.toString(transformer.transform("Doh, John Doe")));

		assertEquals("[Kasper, Sørensen, null]",
				Arrays.toString(transformer.transform("Kasper Sørensen")));

		assertEquals("[Kasper, Sørensen, null]",
				Arrays.toString(transformer.transform("Sørensen, Kasper")));
	}
}
