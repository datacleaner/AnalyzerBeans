package org.eobjects.analyzer.test;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.HsqldbCollectionProvider;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleSynonym;
import org.eobjects.analyzer.reference.SimpleSynonymCatalog;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.junit.Ignore;

@Ignore
public final class TestHelper {

	private static final DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
			"org.eobjects.analyzer.beans", true).scanPackage("org.eobjects.analyzer.result.renderer", true);

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration(Datastore datastore) {
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		return createAnalyzerBeansConfiguration(taskRunner, datastore);
	}

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration() {
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		CollectionProvider collectionProvider = createCollectionProvider();
		return new AnalyzerBeansConfigurationImpl(createDatastoreCatalog(), createReferenceDataCatalog(),
				descriptorProvider, taskRunner, collectionProvider);
	}

	public static CollectionProvider createCollectionProvider() {
		return new HsqldbCollectionProvider();
	}

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration(TaskRunner taskRunner, Datastore datastore) {
		CollectionProvider collectionProvider = createCollectionProvider();

		List<Datastore> datastores = new LinkedList<Datastore>();
		datastores.add(datastore);

		return new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(datastores), createReferenceDataCatalog(),
				descriptorProvider, taskRunner, collectionProvider);
	}

	public static ReferenceDataCatalog createReferenceDataCatalog() {
		Collection<Dictionary> dictionaries = new ArrayList<Dictionary>();
		dictionaries.add(new SimpleDictionary("eobjects.org products", "MetaModel", "DataCleaner", "AnalyzerBeans"));
		dictionaries.add(new SimpleDictionary("apache products", "commons-lang", "commons-math", "commons-codec",
				"commons-logging"));
		dictionaries.add(new SimpleDictionary("logging products", "commons-logging", "log4j", "slf4j", "java.util.Logging"));
		Collection<SynonymCatalog> synonymCatalogs = new ArrayList<SynonymCatalog>();

		synonymCatalogs.add(new SimpleSynonymCatalog("translated terms", new SimpleSynonym("hello", "howdy", "hi", "yo",
				"hey"), new SimpleSynonym("goodbye", "bye", "see you", "hey")));

		return new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs);
	}

	public static DatastoreCatalog createDatastoreCatalog() {
		List<Datastore> datastores = new LinkedList<Datastore>();
		return new DatastoreCatalogImpl(datastores);
	}

	public static JdbcDatastore createSampleDatabaseDatastore(String name) {
		return new JdbcDatastore(name, "jdbc:hsqldb:res:metamodel", "org.hsqldb.jdbcDriver");
	}
}
