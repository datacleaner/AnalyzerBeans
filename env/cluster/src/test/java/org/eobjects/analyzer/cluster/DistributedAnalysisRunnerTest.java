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
package org.eobjects.analyzer.cluster;

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.cluster.virtual.VirtualClusterManager;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.test.TestHelper;

public class DistributedAnalysisRunnerTest extends TestCase {

    public void testVanillaScenarioSingleSlave() throws Throwable {
        final AnalyzerBeansConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        ClusterTestHelper.runConcatAndInsertJob(configuration, new VirtualClusterManager(configuration, 1));
    }

    public void testVanillaScenarioFourSlaves() throws Throwable {
        final AnalyzerBeansConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        ClusterTestHelper.runConcatAndInsertJob(configuration, new VirtualClusterManager(configuration, 4));
    }

    public void testErrorHandlingSingleSlave() throws Exception {
        final AnalyzerBeansConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), false);

        List<Throwable> errors = ClusterTestHelper.runErrorHandlingJob(configuration, new VirtualClusterManager(
                configuration, 1));

        assertEquals("I am just a dummy transformer!", errors.get(0).getMessage());
        assertEquals("A previous exception has occurred", errors.get(1).getMessage());
        assertEquals(2, errors.size());
    }

    public void testErrorHandlingFourSlaves() throws Exception {
        final AnalyzerBeansConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        List<Throwable> errors = ClusterTestHelper.runErrorHandlingJob(configuration, new VirtualClusterManager(
                configuration, 4));

        for (Throwable throwable : errors) {
            String message = throwable.getMessage();
            if (!"I am just a dummy transformer!".equals(message)
                    && !"A previous exception has occurred".equals(message)) {
                fail("Unexpected exception: " + message + " (" + throwable.getClass().getName() + ")");
            }
        }

        // there might be (a lot) more than 8 errors since each node was
        // multi-threaded
        assertTrue(errors.size() >= 8);
    }

    public void testUndistributableAnalyzer() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
                .replace(new DatastoreCatalogImpl(datastore));

        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNAME");

        // The String Analyzer is (currently) not distributable
        final AnalyzerJobBuilder<StringAnalyzer> analyzer = jobBuilder.addAnalyzer(StringAnalyzer.class);
        analyzer.addInputColumns(jobBuilder.getSourceColumns());

        AnalysisJob job = jobBuilder.toAnalysisJob();

        DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, new VirtualClusterManager(
                configuration, 2));

        try {
            runner.run(job);
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals("Component is not distributable: "
                    + "ImmutableAnalyzerJob[name=null,analyzer=String analyzer]", e.getMessage());
        }
    }
}
