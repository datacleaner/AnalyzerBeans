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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.NotNullFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.PrefixedIdGenerator;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.analyzer.test.TestHelper;

import org.eobjects.metamodel.MetaModelTestCase;
import org.eobjects.metamodel.schema.Table;

public class AnalysisJobBuilderTest extends MetaModelTestCase {

	private AnalysisJobBuilder analysisJobBuilder;
	private AnalyzerBeansConfigurationImpl configuration;
	private JdbcDatastore datastore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Collection<Datastore> datastores = new LinkedList<Datastore>();

		datastore = TestHelper.createSampleDatabaseDatastore("my db");
		datastores.add(datastore);

		DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastores);
		ReferenceDataCatalog referenceDataCatalog = new ReferenceDataCatalogImpl();
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
				"org.eobjects.analyzer.beans", true);
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		StorageProvider storageProvider = TestHelper.createStorageProvider();
		configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog, referenceDataCatalog, descriptorProvider,
				taskRunner, storageProvider);

		analysisJobBuilder = new AnalysisJobBuilder(configuration);
		analysisJobBuilder.setDatastore("my db");
	}

	public void testToString() throws Exception {
		RowProcessingAnalyzerJobBuilder<StringAnalyzer> ajb = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		TransformerJobBuilder<ConvertToStringTransformer> tjb = analysisJobBuilder
				.addTransformer(ConvertToStringTransformer.class);

		assertEquals("RowProcessingAnalyzerJobBuilder[analyzer=String analyzer,inputColumns=[]]", ajb.toString());
		assertEquals("TransformerJobBuilder[transformer=Convert to string,inputColumns=[]]", tjb.toString());
	}

	public void testToAnalysisJob() throws Exception {
		Table employeeTable = datastore.getDataContextProvider().getDataContext().getDefaultSchema()
				.getTableByName("EMPLOYEES");
		assertNotNull(employeeTable);

		analysisJobBuilder.addSourceColumns(employeeTable.getColumnByName("EMPLOYEENUMBER"),
				employeeTable.getColumnByName("FIRSTNAME"), employeeTable.getColumnByName("EMAIL"));

		TransformerJobBuilder<ConvertToStringTransformer> transformerJobBuilder = analysisJobBuilder
				.addTransformer(ConvertToStringTransformer.class);

		Collection<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.NUMBER);
		assertEquals(1, numberColumns.size());
		assertEquals("[MetaModelInputColumn[PUBLIC.EMPLOYEES.EMPLOYEENUMBER]]", Arrays.toString(numberColumns.toArray()));

		transformerJobBuilder.addInputColumn(numberColumns.iterator().next());
		assertTrue(transformerJobBuilder.isConfigured());

		// the AnalyzerJob has no Analyzers yet, so it is not "configured".
		assertFalse(analysisJobBuilder.isConfigured());

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> analyzerJobBuilder = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);

		List<InputColumn<?>> stringInputColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.STRING);
		Set<String> columnNames = new TreeSet<String>();
		for (InputColumn<?> inputColumn : stringInputColumns) {
			columnNames.add(inputColumn.getName());
		}
		assertEquals("[EMAIL, EMPLOYEENUMBER (as string), FIRSTNAME]", columnNames.toString());

		analyzerJobBuilder.addInputColumns(stringInputColumns);
		assertTrue(analyzerJobBuilder.isConfigured());

		// now there is: source columns, configured analyzers and configured
		// transformers.
		assertTrue(analysisJobBuilder.isConfigured());

		AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();

		assertEquals(
				"ImmutableAnalysisJob[sourceColumns=3,filterJobs=0,transformerJobs=1,analyzerJobs=1,mergedOutcomeJobs=0]",
				analysisJob.toString());

		// test hashcode and equals
		assertNotSame(analysisJobBuilder.toAnalysisJob(), analysisJob);
		assertEquals(analysisJobBuilder.toAnalysisJob(), analysisJob);
		assertEquals(analysisJobBuilder.toAnalysisJob().hashCode(), analysisJob.hashCode());

		Collection<InputColumn<?>> sourceColumns = analysisJob.getSourceColumns();
		assertEquals(3, sourceColumns.size());

		try {
			sourceColumns.add(new TransformedInputColumn<Boolean>("bla", DataTypeFamily.BOOLEAN, new PrefixedIdGenerator(
					"mock")));
			fail("Exception expected");
		} catch (UnsupportedOperationException e) {
			// do nothing
		}

		Collection<TransformerJob> transformerJobs = analysisJob.getTransformerJobs();
		assertEquals(1, transformerJobs.size());

		TransformerJob transformerJob = transformerJobs.iterator().next();
		assertEquals("ImmutableTransformerJob[name=null,transformer=Convert to string]", transformerJob.toString());

		assertEquals("[MetaModelInputColumn[PUBLIC.EMPLOYEES.EMPLOYEENUMBER]]", Arrays.toString(transformerJob.getInput()));

		Collection<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();
		assertEquals(1, analyzerJobs.size());

		AnalyzerJob analyzerJob = analyzerJobs.iterator().next();
		assertEquals("ImmutableAnalyzerJob[name=null,analyzer=String analyzer]", analyzerJob.toString());
	}

	public void testGetAvailableUnfilteredBeans() throws Exception {
		Table customersTable = datastore.getDataContextProvider().getDataContext().getDefaultSchema()
				.getTableByName("CUSTOMERS");
		assertNotNull(customersTable);

		analysisJobBuilder.addSourceColumns(customersTable.getColumnByName("ADDRESSLINE1"),
				customersTable.getColumnByName("ADDRESSLINE2"));

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> saAjb = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		saAjb.addInputColumns(analysisJobBuilder.getSourceColumns());

		FilterJobBuilder<NotNullFilter, ValidationCategory> fjb = analysisJobBuilder.addFilter(NotNullFilter.class);
		fjb.addInputColumn(analysisJobBuilder.getSourceColumns().get(0));

		List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
		assertEquals(1, result.size());
		assertEquals(result.get(0), saAjb);

		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> pfAjb = analysisJobBuilder
				.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);
		pfAjb.addInputColumns(analysisJobBuilder.getSourceColumns());

		result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
		assertEquals(2, result.size());
		assertEquals(result.get(0), saAjb);
		assertEquals(result.get(1), pfAjb);

		pfAjb.setRequirement(fjb, ValidationCategory.VALID);

		result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
		assertEquals(1, result.size());
		assertEquals(result.get(0), saAjb);
	}

	public void testRemoveFilter() throws Exception {
		DataContextProvider dcp = datastore.getDataContextProvider();

		FilterJobBuilder<MaxRowsFilter, ValidationCategory> maxRowsFilter = analysisJobBuilder
				.addFilter(MaxRowsFilter.class);
		analysisJobBuilder.setDefaultRequirement(maxRowsFilter, ValidationCategory.VALID);

		TransformerJobBuilder<EmailStandardizerTransformer> emailStdTransformer = analysisJobBuilder
				.addTransformer(EmailStandardizerTransformer.class);
		assertSame(maxRowsFilter.getOutcome(ValidationCategory.VALID), emailStdTransformer.getRequirement());

		FilterJobBuilder<NotNullFilter, ValidationCategory> notNullFilter = analysisJobBuilder
				.addFilter(NotNullFilter.class);
		notNullFilter.setRequirement(null);
		maxRowsFilter.setRequirement(notNullFilter.getOutcome(ValidationCategory.VALID));

		assertNull(notNullFilter.getRequirement());

		analysisJobBuilder.addSourceColumn(dcp.getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL"));
		emailStdTransformer.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> stringAnalyzer = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		stringAnalyzer.addInputColumns(emailStdTransformer.getOutputColumns());
		
		assertSame(maxRowsFilter.getOutcome(ValidationCategory.VALID), stringAnalyzer.getRequirement());
		
		analysisJobBuilder.removeFilter(maxRowsFilter);
		
		assertNull(analysisJobBuilder.getDefaultRequirement());
		assertSame(notNullFilter.getOutcome(ValidationCategory.VALID), stringAnalyzer.getRequirement());
		assertSame(notNullFilter.getOutcome(ValidationCategory.VALID), emailStdTransformer.getRequirement());

		dcp.close();
	}
}
