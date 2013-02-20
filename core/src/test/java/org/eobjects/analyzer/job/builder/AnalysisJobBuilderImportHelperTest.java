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

import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.test.MockAnalyzer;
import org.eobjects.analyzer.test.MockTransformer;
import org.eobjects.analyzer.test.TestHelper;

import junit.framework.TestCase;

public class AnalysisJobBuilderImportHelperTest extends TestCase {

    public void testImportRenamedTransformedColumn() throws Exception {
        // build a job with a renamed transformer output column
        Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(
                datastore));

        final AnalysisJob originalJob;
        {
            AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf);
            jobBuilder.setDatastore(datastore);
            jobBuilder.addSourceColumns("EMPLOYEES.FIRSTNAME");

            TransformerJobBuilder<MockTransformer> transformer = jobBuilder.addTransformer(MockTransformer.class);
            transformer.addInputColumn(jobBuilder.getSourceColumnByName("FIRSTNAME"));
            List<MutableInputColumn<?>> columns = transformer.getOutputColumns();
            assertEquals("[TransformedInputColumn[id=trans-1,name=mock output]]", columns.toString());

            MutableInputColumn<?> renamedColumn = columns.get(0);
            renamedColumn.setName("foobar");

            jobBuilder.addAnalyzer(MockAnalyzer.class).addInputColumn(renamedColumn);

            originalJob = jobBuilder.toAnalysisJob();
        }
        
        AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf, originalJob);
        
        List<TransformerJobBuilder<?>> transformers = jobBuilder.getTransformerJobBuilders();
        assertEquals(1, transformers.size());
        
        List<MutableInputColumn<?>> outputColumns = transformers.get(0).getOutputColumns();
        assertEquals("foobar", outputColumns.get(0).getName());
    }
}
