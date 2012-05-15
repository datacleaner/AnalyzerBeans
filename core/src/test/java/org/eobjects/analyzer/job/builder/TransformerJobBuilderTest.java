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

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.mock.TransformerMock;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.data.ConstantInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
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
        TransformerJobBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        try {
            tjb.setConfiguredProperty("Input", "hello");
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid value type: java.lang.String, expected: org.eobjects.analyzer.data.InputColumn", e.getMessage());
        }
    }

    public void testIsConfigured() throws Exception {
        TransformerJobBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        assertFalse(tjb.isConfigured());
        
        tjb.setConfiguredProperty("Some integer", null);

        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        assertFalse(tjb.isConfigured());
        
        try {
            tjb.isConfigured(true);
            fail("Exception occurred");
        } catch (UnconfiguredConfiguredPropertyException e) {
            assertEquals("Property 'Some integer' is not properly configured", e.getMessage());
        }

        tjb.setConfiguredProperty("Some integer", 10);
        assertTrue(tjb.isConfigured());

        tjb.removeInputColumn(ajb.getSourceColumns().get(1));
        assertFalse(tjb.isConfigured());
    }

    public void testClearInputColumnsArray() throws Exception {
        TransformerJobBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        tjb.addInputColumn(new ConstantInputColumn("foo"));

        assertEquals(2, tjb.getInputColumns().size());

        tjb.clearInputColumns();

        assertEquals(0, tjb.getInputColumns().size());
    }

    public void testClearInputColumnsSingle() throws Exception {
        TransformerJobBuilder<SingleInputColumnTransformer> tjb = ajb
                .addTransformer(SingleInputColumnTransformer.class);
        tjb.addInputColumn(ajb.getSourceColumns().get(1));

        assertEquals(1, tjb.getInputColumns().size());

        tjb.clearInputColumns();

        assertEquals(0, tjb.getInputColumns().size());
    }

    public void testGetAvailableInputColumns() throws Exception {
        assertEquals(2, ajb.getAvailableInputColumns(Object.class).size());
        assertEquals(2, ajb.getAvailableInputColumns((Class<?>) null).size());
        assertEquals(1, ajb.getAvailableInputColumns(String.class).size());
        assertEquals(0, ajb.getAvailableInputColumns(Date.class).size());
    }

    public void testInvalidInputColumnType() throws Exception {
        TransformerJobBuilder<SingleInputColumnTransformer> tjb = ajb
                .addTransformer(SingleInputColumnTransformer.class);
        assertEquals(0, tjb.getInputColumns().size());
        assertFalse(tjb.isConfigured());
        try {
            tjb.addInputColumn(ajb.getSourceColumns().get(0));
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported InputColumn type: class java.lang.Integer, expected: class java.lang.String",
                    e.getMessage());
        }
        assertFalse(tjb.isConfigured());

        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        assertEquals(1, tjb.getInputColumns().size());
        assertTrue(tjb.isConfigured());
    }

    public void testNoOutputWhenNotConfigured() throws Exception {
        TransformerJobBuilder<SingleInputColumnTransformer> tjb = ajb
                .addTransformer(SingleInputColumnTransformer.class);

        // not yet configured
        assertEquals(0, tjb.getOutputColumns().size());

        tjb.addInputColumn(new MockInputColumn<String>("email", String.class));

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
        InputColumn<String> input = new MockInputColumn<String>("foo", String.class);
        configurableBean.setInput(input);

        assertTrue(builder.isConfigured());
        ConfiguredPropertyDescriptor propertyDescriptor = descriptor.getConfiguredPropertiesForInput().iterator()
                .next();
        InputColumn<?>[] value = (InputColumn<?>[]) builder.getConfiguredProperties().get(propertyDescriptor);
        assertEquals("[MockInputColumn[name=foo]]", Arrays.toString(value));
    }

    public void testReplaceAutomaticOutputColumnNames() throws Exception {
        IdGenerator IdGenerator = new PrefixedIdGenerator("id");

        TransformerBeanDescriptor<TransformerMock> descriptor = Descriptors.ofTransformer(TransformerMock.class);

        TransformerJobBuilder<TransformerMock> builder = new TransformerJobBuilder<TransformerMock>(
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
        assertEquals(
                "[TransformedInputColumn[id=id-1,name=Transformer mock (1)], TransformedInputColumn[id=id-2,name=Transformer mock (2)], TransformedInputColumn[id=id-3,name=Transformer mock (3)]]",
                outputColumns.toString());

        outputColumns.get(0).setName("Foo A");

        builder.removeInputColumn(colB);

        outputColumns = builder.getOutputColumns();
        assertEquals(2, outputColumns.size());
        assertEquals(
                "[TransformedInputColumn[id=id-1,name=Foo A], TransformedInputColumn[id=id-2,name=Transformer mock (2)]]",
                outputColumns.toString());

        builder.addInputColumn(colB);
        outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals(
                "[TransformedInputColumn[id=id-1,name=Foo A], TransformedInputColumn[id=id-2,name=Transformer mock (2)], TransformedInputColumn[id=id-4,name=Transformer mock (3)]]",
                outputColumns.toString());

        ConfiguredPropertyDescriptor inputColumnProperty = descriptor.getConfiguredPropertiesForInput().iterator()
                .next();
        builder.setConfiguredProperty(inputColumnProperty, new InputColumn[] { colA, colB, colC });
        outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals(
                "[TransformedInputColumn[id=id-1,name=Foo A], TransformedInputColumn[id=id-2,name=Transformer mock (2)], TransformedInputColumn[id=id-4,name=Transformer mock (3)]]",
                outputColumns.toString());

        assertEquals("Transformer mock (1)", outputColumns.get(0).getInitialName());
    }
}
