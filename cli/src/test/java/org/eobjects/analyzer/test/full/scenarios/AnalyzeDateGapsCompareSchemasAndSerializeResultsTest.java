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
package org.eobjects.analyzer.test.full.scenarios;

import java.util.Collection;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.beans.CompareSchemasAnalyzer;
import org.eobjects.analyzer.beans.DateGapAnalyzer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.ExplorerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.util.ObjectComparator;

public class AnalyzeDateGapsCompareSchemasAndSerializeResultsTest extends TestCase {

	@SuppressWarnings("unchecked")
	public void testScenario() throws Throwable {
		final AnalyzerBeansConfiguration configuration;
		{
			// create configuration
			SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
			descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(DateGapAnalyzer.class));
			descriptorProvider.addExplorerBeanDescriptor(Descriptors.ofExplorer(CompareSchemasAnalyzer.class));
			descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(MaxRowsFilter.class));
			descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConvertToStringTransformer.class));
			JdbcDatastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
			configuration = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider).replace(
					new DatastoreCatalogImpl(datastore));
		}

		AnalysisJob job;
		{
			// create job
			AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
			Datastore datastore = configuration.getDatastoreCatalog().getDatastore("orderdb");
			analysisJobBuilder.setDatastore(datastore);
			analysisJobBuilder.addSourceColumns("PUBLIC.ORDERS.ORDERDATE", "PUBLIC.ORDERS.SHIPPEDDATE",
					"PUBLIC.ORDERS.CUSTOMERNUMBER");
			assertEquals(3, analysisJobBuilder.getSourceColumns().size());

			FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> maxRows = analysisJobBuilder.addFilter(MaxRowsFilter.class);
			maxRows.getConfigurableBean().setMaxRows(5);
			analysisJobBuilder.setDefaultRequirement(maxRows.getOutcome(MaxRowsFilter.Category.VALID));

			TransformerJobBuilder<ConvertToStringTransformer> convertToNumber = analysisJobBuilder
					.addTransformer(ConvertToStringTransformer.class);
			convertToNumber.addInputColumn(analysisJobBuilder.getSourceColumnByName("customernumber"));
			InputColumn<String> customer_no = (InputColumn<String>) convertToNumber.getOutputColumns().get(0);

			AnalyzerJobBuilder<DateGapAnalyzer> dateGap = analysisJobBuilder.addAnalyzer(DateGapAnalyzer.class);
			dateGap.setName("date gap job");
			dateGap.getConfigurableBean().setSingleDateOverlaps(true);
			dateGap.getConfigurableBean().setFromColumn(
					(InputColumn<Date>) analysisJobBuilder.getSourceColumnByName("orderdate"));
			dateGap.getConfigurableBean().setToColumn(
					(InputColumn<Date>) analysisJobBuilder.getSourceColumnByName("shippeddate"));
			dateGap.getConfigurableBean().setGroupColumn(customer_no);

			ExplorerJobBuilder<CompareSchemasAnalyzer> compareSchemas = analysisJobBuilder
					.addExplorer(CompareSchemasAnalyzer.class);
			DatastoreConnection con = datastore.openConnection();
			Schema[] schemas = con.getSchemaNavigator().getSchemas();
			assertEquals(2, schemas.length);
			compareSchemas.setConfiguredProperty("First schema", schemas[0]);
			compareSchemas.setConfiguredProperty("Second schema", schemas[1]);
			con.close();

			job = analysisJobBuilder.toAnalysisJob();
		}

		AnalysisResultFuture future = new AnalysisRunnerImpl(configuration).run(job);
		if (future.isErrornous()) {
			throw future.getErrors().get(0);
		}
		assertTrue(future.isSuccessful());

		SimpleAnalysisResult result1 = new SimpleAnalysisResult(future.getResultMap());
		byte[] bytes = SerializationUtils.serialize(result1);
		SimpleAnalysisResult result2 = (SimpleAnalysisResult) SerializationUtils.deserialize(bytes);

		performResultAssertions(job, future);
		performResultAssertions(job, result1);
		performResultAssertions(job, result2);
	}

	private void performResultAssertions(AnalysisJob job, AnalysisResult result) {
		assertEquals(2, result.getResults().size());

		Collection<ComponentJob> componentJobs = result.getResultMap().keySet();
		componentJobs = CollectionUtils2.sorted(componentJobs, ObjectComparator.getComparator());

		assertEquals(
				"[ImmutableAnalyzerJob[name=date gap job,analyzer=Date gap analyzer], ImmutableExplorerJob[name=null,explorer=Compare schema structures]]",
				componentJobs.toString());

		// using the original component jobs not only asserts that these exist
		// in the result, but also that the their deserialized clones are equal
		// (otherwise the results cannot be retrieved from the result map).
		final AnalyzerJob analyzerJob = job.getAnalyzerJobs().iterator().next();
		final ExplorerJob explorerJob = job.getExplorerJobs().iterator().next();

		final AnalyzerResult analyzerResult = result.getResult(analyzerJob);
		assertNotNull(analyzerResult);
		assertEquals("DateGapAnalyzerResult[gaps={121=[], 128=[], 141=[], 181=[], 363=[]}]", analyzerResult.toString());

		final AnalyzerResult explorerResult = result.getResult(explorerJob);
		assertNotNull(explorerResult);
		assertEquals(
				"SchemaComparisonResult[differences=["
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'name': [INFORMATION_SCHEMA] vs. [PUBLIC], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [CUSTOMERS], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [CUSTOMER_W_TER], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [DEPARTMENT_MANAGERS], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [DIM_TIME], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [EMPLOYEES], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [OFFICES], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [ORDERDETAILS], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [ORDERFACT], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [ORDERS], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [PAYMENTS], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [PRODUCTS], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [QUADRANT_ACTUALS], "
						+ "Schemas 'INFORMATION_SCHEMA' and 'PUBLIC' differ on 'unmatched table': [null] vs. [TRIAL_BALANCE]], "
						+ "table comparison=[]]", explorerResult.toString());
	}

}
