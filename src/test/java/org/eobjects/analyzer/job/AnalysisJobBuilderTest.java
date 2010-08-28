package org.eobjects.analyzer.job;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.ConvertToStringTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.InMemoryCollectionProvider;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.schema.Table;

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
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.beans", true);
		TaskRunner taskRunner = new SingleThreadedTaskRunner(false);
		CollectionProvider collectionProvider = new InMemoryCollectionProvider();
		configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog,
				referenceDataCatalog, descriptorProvider, taskRunner,
				collectionProvider);

		analysisJobBuilder = new AnalysisJobBuilder(configuration);
	}

	public void testToString() throws Exception {
		RowProcessingAnalyzerJobBuilder<StringAnalyzer> ajb = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		TransformerJobBuilder<ConvertToStringTransformer> tjb = analysisJobBuilder
				.addTransformer(ConvertToStringTransformer.class);

		assertEquals(
				"RowProcessingAnalyzerJobBuilder[analyzer=String analyzer,inputColumns=[]]",
				ajb.toString());
		assertEquals(
				"TransformerJobBuilder[transformer=Convert to string,inputColumns=[]]",
				tjb.toString());
	}

	public void testToAnalysisJob() throws Exception {
		analysisJobBuilder.setDatastore("my db");
		Table employeeTable = datastore.getDataContextProvider()
				.getDataContext().getDefaultSchema()
				.getTableByName("EMPLOYEES");
		assertNotNull(employeeTable);

		analysisJobBuilder.addSourceColumns(
				employeeTable.getColumnByName("EMPLOYEENUMBER"),
				employeeTable.getColumnByName("FIRSTNAME"),
				employeeTable.getColumnByName("EMAIL"));

		TransformerJobBuilder<ConvertToStringTransformer> transformerJobBuilder = analysisJobBuilder
				.addTransformer(ConvertToStringTransformer.class);

		Collection<InputColumn<?>> numberColumns = analysisJobBuilder
				.getAvailableInputColumns(DataTypeFamily.NUMBER);
		assertEquals(1, numberColumns.size());
		assertEquals(
				"[MetaModelInputColumn[JdbcColumn[name=EMPLOYEENUMBER,columnNumber=0,type=INTEGER,nullable=false,indexed=true,nativeType=INTEGER,columnSize=0]]]",
				Arrays.toString(numberColumns.toArray()));

		transformerJobBuilder.addInputColumn(numberColumns.iterator().next());
		assertTrue(transformerJobBuilder.isConfigured());

		// the AnalyzerJob has no Analyzers yet, so it is not "configured".
		assertFalse(analysisJobBuilder.isConfigured());

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> analyzerJobBuilder = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);

		Collection<InputColumn<?>> stringInputColumns = analysisJobBuilder
				.getAvailableInputColumns(DataTypeFamily.STRING);
		assertEquals(
				"[MetaModelInputColumn[JdbcColumn[name=FIRSTNAME,columnNumber=2,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]], "
						+ "MetaModelInputColumn[JdbcColumn[name=EMAIL,columnNumber=4,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=100]], "
						+ "TransformedInputColumn[id=trans-1,name=Convert to string 1,type=STRING]]",
				Arrays.toString(stringInputColumns.toArray()));

		analyzerJobBuilder.addInputColumns(stringInputColumns);
		assertTrue(analyzerJobBuilder.isConfigured());

		// now there is: source columns, configured analyzers and configured
		// transformers.
		assertTrue(analysisJobBuilder.isConfigured());

		AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();
		assertEquals(
				"ImmutableAnalysisJob[sourceColumns=3,transformerJobs=1,analyzerJobs=1]",
				analysisJob.toString());

		// test hashcode and equals
		assertNotSame(analysisJobBuilder.toAnalysisJob(), analysisJob);
		assertEquals(analysisJobBuilder.toAnalysisJob(), analysisJob);
		assertEquals(analysisJobBuilder.toAnalysisJob().hashCode(),
				analysisJob.hashCode());

		Collection<InputColumn<?>> sourceColumns = analysisJob
				.getSourceColumns();
		assertEquals(3, sourceColumns.size());

		try {
			sourceColumns.add(new TransformedInputColumn<Boolean>("bla",
					DataTypeFamily.BOOLEAN, new PrefixedIdGenerator("mock")));
			fail("Exception expected");
		} catch (UnsupportedOperationException e) {
			// do nothing
		}

		Collection<TransformerJob> transformerJobs = analysisJob
				.getTransformerJobs();
		assertEquals(1, transformerJobs.size());

		TransformerJob transformerJob = transformerJobs.iterator().next();
		assertEquals("ImmutableTransformerJob[transformer=Convert to string]",
				transformerJob.toString());

		assertEquals(
				"[MetaModelInputColumn[JdbcColumn[name=EMPLOYEENUMBER,columnNumber=0,type=INTEGER,nullable=false,indexed=true,nativeType=INTEGER,columnSize=0]]]",
				Arrays.toString(transformerJob.getInput()));

		Collection<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();
		assertEquals(1, analyzerJobs.size());

		AnalyzerJob analyzerJob = analyzerJobs.iterator().next();
		assertEquals("ImmutableAnalyzerJob[analyzer=String analyzer]",
				analyzerJob.toString());
	}
}
