package org.eobjects.analyzer.test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.SynonymCatalog;

public final class TestHelper {

	private static DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
			.scanPackage("org.eobjects.analyzer.beans", true).scanPackage(
					"org.eobjects.analyzer.result.renderer", true);

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration(
			Datastore datastore) {
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		CollectionProvider collectionProvider = new BerkeleyDbCollectionProvider();

		List<Datastore> datastores = new LinkedList<Datastore>();
		datastores.add(datastore);

		return new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(
				datastores), createReferenceDataCatalog(), descriptorProvider,
				taskRunner, collectionProvider);
	}

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration() {
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		CollectionProvider collectionProvider = new BerkeleyDbCollectionProvider();
		return new AnalyzerBeansConfigurationImpl(createDatastoreCatalog(),
				createReferenceDataCatalog(), descriptorProvider, taskRunner,
				collectionProvider);
	}

	public static ReferenceDataCatalog createReferenceDataCatalog() {
		Collection<Dictionary> dictionaries = Collections.emptyList();
		Collection<SynonymCatalog> synonymCatalogs = Collections.emptyList();
		return new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs);
	}

	public static DatastoreCatalog createDatastoreCatalog() {
		List<Datastore> datastores = new LinkedList<Datastore>();
		return new DatastoreCatalogImpl(datastores);
	}

	public static JdbcDatastore createSampleDatabaseDatastore(String name) {
		return new JdbcDatastore(name, "jdbc:hsqldb:res:metamodel",
				"org.hsqldb.jdbcDriver");
	}
}
