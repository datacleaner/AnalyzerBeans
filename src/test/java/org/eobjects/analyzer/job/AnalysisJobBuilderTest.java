package org.eobjects.analyzer.job;

import java.util.Collection;
import java.util.LinkedList;

import org.eobjects.analyzer.beans.StringConverterTransformer;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
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

	private AnalysisJobBuilder ajb;
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

		ajb = new AnalysisJobBuilder(configuration);
	}

	public void testToAnalysisJob() throws Exception {
		ajb.setDatastore("my db");
		Table employeeTable = datastore.getDataContextProvider()
				.getDataContext().getDefaultSchema()
				.getTableByName("EMPLOYEES");
		assertNotNull(employeeTable);

		ajb.addSourceColumns(employeeTable.getColumns());

		TransformerJobBuilder tjb = ajb
				.addTransformer(StringConverterTransformer.class);

		Collection<InputColumn<?>> numberColumns = ajb
				.getAvailableInputColumns(DataTypeFamily.NUMBER);
		assertEquals(2, numberColumns.size());
		
		tjb.addInputColumn(numberColumns.iterator().next());
		assertTrue(tjb.isConfigured());
		
		// TODO: Expand testcase
	}
}
