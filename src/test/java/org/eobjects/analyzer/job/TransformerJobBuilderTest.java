package org.eobjects.analyzer.job;

import org.eobjects.analyzer.beans.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.TokenizerTransformer;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

import junit.framework.TestCase;

public class TransformerJobBuilderTest extends TestCase {

	private AnalyzerBeansConfiguration configuration;
	private AnalysisJobBuilder ajb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		configuration = TestHelper.createAnalyzerBeansConfiguration();

		ajb = new AnalysisJobBuilder(configuration);

		ajb.addSourceColumn(new Column("fooInt", ColumnType.INTEGER));
		ajb.addSourceColumn(new Column("fooStr", ColumnType.VARCHAR));
	}

	public void testSetInvalidPropertyType() throws Exception {
		TransformerJobBuilder<TokenizerTransformer> tjb = ajb
				.addTransformer(TokenizerTransformer.class);
		try {
			tjb.setConfiguredProperty("Number of tokens", "hello");
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Invalid value type: java.lang.String, expected: java.lang.Integer",
					e.getMessage());
		}
	}

	public void testIsConfigured() throws Exception {
		TransformerJobBuilder<TokenizerTransformer> tjb = ajb
				.addTransformer(TokenizerTransformer.class);
		assertFalse(tjb.isConfigured());

		tjb.addInputColumn(ajb.getSourceColumns().get(1));
		assertFalse(tjb.isConfigured());

		tjb.setConfiguredProperty("Number of tokens", 10);
		assertTrue(tjb.isConfigured());

		tjb.removeInputColumn(ajb.getSourceColumns().get(1));
		assertFalse(tjb.isConfigured());
	}

	public void testGetAvailableInputColumns() throws Exception {
		assertEquals(2, ajb.getAvailableInputColumns(DataTypeFamily.UNDEFINED)
				.size());
		assertEquals(2, ajb.getAvailableInputColumns(null).size());
		assertEquals(1, ajb.getAvailableInputColumns(DataTypeFamily.STRING)
				.size());
		assertEquals(0, ajb.getAvailableInputColumns(DataTypeFamily.DATE)
				.size());
	}

	public void testInvalidInputColumnType() throws Exception {
		TransformerJobBuilder<ConvertToNumberTransformer> tjb = ajb
				.addTransformer(ConvertToNumberTransformer.class);
		assertEquals(1, tjb.getOutputColumns().size());
		assertEquals(0, tjb.getInputColumns().size());
		assertFalse(tjb.isConfigured());
		try {
			tjb.addInputColumn(ajb.getSourceColumns().get(0));
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Unsupported InputColumn type: NUMBER, expected: STRING",
					e.getMessage());
		}
		assertFalse(tjb.isConfigured());

		tjb.addInputColumn(ajb.getSourceColumns().get(1));
		assertEquals(1, tjb.getInputColumns().size());
		assertTrue(tjb.isConfigured());
	}
}
