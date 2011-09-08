/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job.builder;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.standardize.TokenizerTransformer;
import org.eobjects.analyzer.beans.transform.ConcatenatorTransformer;
import org.eobjects.analyzer.beans.transform.WhitespaceTrimmerTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.data.ConstantInputColumn;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.PrefixedIdGenerator;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.MutableColumn;

public class TransformerJobBuilderTest extends TestCase {

	private AnalyzerBeansConfiguration configuration;
	private AnalysisJobBuilder ajb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		configuration = new AnalyzerBeansConfigurationImpl();

		ajb = new AnalysisJobBuilder(configuration);

		ajb.addSourceColumn(new MutableColumn("fooInt", ColumnType.INTEGER));
		ajb.addSourceColumn(new MutableColumn("fooStr", ColumnType.VARCHAR));
	}

	public void testSetInvalidPropertyType() throws Exception {
		TransformerJobBuilder<TokenizerTransformer> tjb = ajb.addTransformer(TokenizerTransformer.class);
		try {
			tjb.setConfiguredProperty("Number of tokens", "hello");
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value type: java.lang.String, expected: java.lang.Integer", e.getMessage());
		}
	}

	public void testIsConfigured() throws Exception {
		TransformerJobBuilder<TokenizerTransformer> tjb = ajb.addTransformer(TokenizerTransformer.class);
		assertFalse(tjb.isConfigured());

		tjb.addInputColumn(ajb.getSourceColumns().get(1));
		assertFalse(tjb.isConfigured());

		try {
			tjb.isConfigured(true);
			fail("Exception occurred");
		} catch (UnconfiguredConfiguredPropertyException e) {
			assertEquals("Property 'Number of tokens' is not properly configured", e.getMessage());
		}

		tjb.setConfiguredProperty("Number of tokens", 10);
		assertTrue(tjb.isConfigured());

		tjb.removeInputColumn(ajb.getSourceColumns().get(1));
		assertFalse(tjb.isConfigured());
	}

	public void testClearInputColumnsArray() throws Exception {
		TransformerJobBuilder<ConcatenatorTransformer> tjb = ajb.addTransformer(ConcatenatorTransformer.class);
		tjb.addInputColumn(ajb.getSourceColumns().get(1));
		tjb.addInputColumn(new ConstantInputColumn("foo"));

		assertEquals(2, tjb.getInputColumns().size());

		tjb.clearInputColumns();

		assertEquals(0, tjb.getInputColumns().size());
	}

	public void testClearInputColumnsSingle() throws Exception {
		TransformerJobBuilder<EmailStandardizerTransformer> tjb = ajb.addTransformer(EmailStandardizerTransformer.class);
		tjb.addInputColumn(ajb.getSourceColumns().get(1));

		assertEquals(1, tjb.getInputColumns().size());

		tjb.clearInputColumns();

		assertEquals(0, tjb.getInputColumns().size());
	}

	public void testGetAvailableInputColumns() throws Exception {
		assertEquals(2, ajb.getAvailableInputColumns(DataTypeFamily.UNDEFINED).size());
		assertEquals(2, ajb.getAvailableInputColumns(null).size());
		assertEquals(1, ajb.getAvailableInputColumns(DataTypeFamily.STRING).size());
		assertEquals(0, ajb.getAvailableInputColumns(DataTypeFamily.DATE).size());
	}

	public void testInvalidInputColumnType() throws Exception {
		TransformerJobBuilder<EmailStandardizerTransformer> tjb = ajb.addTransformer(EmailStandardizerTransformer.class);
		assertEquals(0, tjb.getInputColumns().size());
		assertFalse(tjb.isConfigured());
		try {
			tjb.addInputColumn(ajb.getSourceColumns().get(0));
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("Unsupported InputColumn type: NUMBER, expected: STRING", e.getMessage());
		}
		assertFalse(tjb.isConfigured());

		tjb.addInputColumn(ajb.getSourceColumns().get(1));
		assertEquals(1, tjb.getInputColumns().size());
		assertTrue(tjb.isConfigured());
	}

	public void testNoOutputWhenNotConfigured() throws Exception {
		TransformerJobBuilder<EmailStandardizerTransformer> tjb = ajb.addTransformer(EmailStandardizerTransformer.class);

		// not yet configured
		assertEquals(0, tjb.getOutputColumns().size());

		tjb.getConfigurableBean().setInputColumn(new MockInputColumn<String>("email", String.class));

		assertEquals(2, tjb.getOutputColumns().size());
	}

	public void testConfigureByConfigurableBean() throws Exception {
		IdGenerator IdGenerator = new PrefixedIdGenerator("");

		TransformerBeanDescriptor<ConvertToNumberTransformer> descriptor = Descriptors
				.ofTransformer(ConvertToNumberTransformer.class);
		TransformerJobBuilder<ConvertToNumberTransformer> builder = new TransformerJobBuilder<ConvertToNumberTransformer>(
				new AnalysisJobBuilder(null), descriptor, IdGenerator, new LinkedList<TransformerChangeListener>());
		assertFalse(builder.isConfigured());

		ConvertToNumberTransformer configurableBean = builder.getConfigurableBean();
		InputColumn<String> input = new TransformedInputColumn<String>("foo", DataTypeFamily.STRING, IdGenerator);
		configurableBean.setInput(input);

		assertTrue(builder.isConfigured());
		ConfiguredPropertyDescriptor propertyDescriptor = descriptor.getConfiguredPropertiesForInput().iterator().next();
		Object object = builder.getConfiguredProperties().get(propertyDescriptor);
		assertEquals("TransformedInputColumn[id=-1,name=foo,type=STRING]", object.toString());
	}

	public void testReplaceAutomaticOutputColumnNames() throws Exception {
		IdGenerator IdGenerator = new PrefixedIdGenerator("id");

		TransformerBeanDescriptor<WhitespaceTrimmerTransformer> descriptor = Descriptors
				.ofTransformer(WhitespaceTrimmerTransformer.class);

		TransformerJobBuilder<WhitespaceTrimmerTransformer> builder = new TransformerJobBuilder<WhitespaceTrimmerTransformer>(
				new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl()), descriptor, IdGenerator,
				new LinkedList<TransformerChangeListener>());

		MockInputColumn<String> colA = new MockInputColumn<String>("A", String.class);
		MockInputColumn<String> colB = new MockInputColumn<String>("B", String.class);
		MockInputColumn<String> colC = new MockInputColumn<String>("C", String.class);
		builder.addInputColumn(colA);
		builder.addInputColumn(colB);
		builder.addInputColumn(colC);

		List<MutableInputColumn<?>> outputColumns = builder.getOutputColumns();
		assertEquals(3, outputColumns.size());
		assertEquals("[TransformedInputColumn[id=id-1,name=A (trimmed),type=STRING], "
				+ "TransformedInputColumn[id=id-2,name=B (trimmed),type=STRING], "
				+ "TransformedInputColumn[id=id-3,name=C (trimmed),type=STRING]]", outputColumns.toString());

		outputColumns.get(0).setName("Foo A");
		
		builder.removeInputColumn(colB);

		outputColumns = builder.getOutputColumns();
		assertEquals(2, outputColumns.size());
		assertEquals("[TransformedInputColumn[id=id-1,name=Foo A,type=STRING], "
				+ "TransformedInputColumn[id=id-2,name=C (trimmed),type=STRING]]", outputColumns.toString());

		builder.addInputColumn(colB);
		outputColumns = builder.getOutputColumns();
		assertEquals(3, outputColumns.size());
		assertEquals("[TransformedInputColumn[id=id-1,name=Foo A,type=STRING], "
				+ "TransformedInputColumn[id=id-2,name=C (trimmed),type=STRING], "
				+ "TransformedInputColumn[id=id-4,name=B (trimmed),type=STRING]]", outputColumns.toString());

		ConfiguredPropertyDescriptor inputColumnProperty = descriptor.getConfiguredPropertiesForInput().iterator().next();
		builder.setConfiguredProperty(inputColumnProperty, new InputColumn[] { colA, colB, colC });
		outputColumns = builder.getOutputColumns();
		assertEquals(3, outputColumns.size());
		assertEquals("[TransformedInputColumn[id=id-1,name=Foo A,type=STRING], "
				+ "TransformedInputColumn[id=id-2,name=B (trimmed),type=STRING], "
				+ "TransformedInputColumn[id=id-4,name=C (trimmed),type=STRING]]", outputColumns.toString());
		
		assertEquals("A (trimmed)", outputColumns.get(0).getInitialName());
	}
}
