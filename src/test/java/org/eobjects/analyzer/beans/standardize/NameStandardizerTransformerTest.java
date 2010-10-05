package org.eobjects.analyzer.beans.standardize;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.standardize.NameStandardizerTransformer;
import org.eobjects.analyzer.data.MetaModelInputColumn;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.MutableColumn;

public class NameStandardizerTransformerTest extends TestCase {

	public void testSimpleScenario() throws Exception {
		NameStandardizerTransformer transformer = new NameStandardizerTransformer();
		Column column = new MutableColumn("name", ColumnType.VARCHAR);

		MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
		transformer.setInputColumn(inputColumn);

		transformer.init();

		assertEquals("[John, Doh, Doe, null]",
				Arrays.toString(transformer.transform("John Doe Doh")));

		assertEquals("[John, Doh, Doe, null]",
				Arrays.toString(transformer.transform("Doh, John Doe")));

		assertEquals("[Kasper, Sørensen, null, null]",
				Arrays.toString(transformer.transform("Kasper Sørensen")));

		assertEquals("[Kasper, Sørensen, null, null]",
				Arrays.toString(transformer.transform("Sørensen, Kasper")));
		
		assertEquals("[Kasper, Sørensen, null, Mr]",
				Arrays.toString(transformer.transform("Mr. Kasper Sørensen")));
		
		assertEquals("[Kasper, Sørensen, null, Mister]",
				Arrays.toString(transformer.transform("Mister Kasper Sørensen")));
		
		assertEquals("[Jane, Foobar, Doe, Mrs]",
				Arrays.toString(transformer.transform("Mrs. Jane Doe Foobar")));
	}
}
