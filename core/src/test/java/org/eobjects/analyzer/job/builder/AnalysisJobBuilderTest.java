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

import java.io.File;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.test.MockFilter;
import org.eobjects.analyzer.test.MockFilter.Category;
import org.eobjects.analyzer.test.MockTransformer;
import org.eobjects.analyzer.test.mock.MockDatastore;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;

public class AnalysisJobBuilderTest extends TestCase {

    /**
     * Builds a scenario with 2 transformers and a filter inbetween. When a
     * filter outcome is set as the default requirement, that requirement should
     * only be set on the succeeding (not preceeding) transformer.
     */
    public void testSetDefaultRequirementNonCyclic() throws Exception {
        MutableTable table = new MutableTable("table");
        MutableColumn column = new MutableColumn("foo").setTable(table);
        table.addColumn(column);

        // set up
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl())) {
            MockDatastore datastore = new MockDatastore();
            ajb.setDatastore(datastore);
            ajb.addSourceColumn(new MetaModelInputColumn(column));

            // add a transformer
            TransformerJobBuilder<MockTransformer> tjb1 = ajb.addTransformer(MockTransformer.class);
            tjb1.addInputColumn(ajb.getSourceColumns().get(0));
            assertTrue(tjb1.isConfigured(true));

            // add filter
            FilterJobBuilder<MockFilter, Category> filter = ajb.addFilter(MockFilter.class);
            filter.addInputColumn(tjb1.getOutputColumns().get(0));
            filter.getConfigurableBean().setSomeEnum(Category.VALID);
            filter.getConfigurableBean().setSomeFile(new File("."));
            assertTrue(filter.isConfigured(true));

            // set default requirement
            ajb.setDefaultRequirement(filter, Category.VALID);

            // add another transformer
            TransformerJobBuilder<MockTransformer> tjb2 = ajb.addTransformer(MockTransformer.class);
            tjb2.addInputColumn(tjb1.getOutputColumns().get(0));
            assertTrue(tjb2.isConfigured(true));

            // assertions
            assertEquals("FilterOutcome[category=VALID]", tjb2.getRequirement().toString());
            assertEquals(null, filter.getRequirement());
            assertEquals(null, tjb1.getRequirement());
        }
    }
}
