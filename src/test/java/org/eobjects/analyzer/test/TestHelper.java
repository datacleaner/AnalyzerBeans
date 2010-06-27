package org.eobjects.analyzer.test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.job.concurrent.ConcurrencyProvider;
import org.eobjects.analyzer.job.concurrent.SingleThreadedConcurrencyProvider;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.SynonymCatalog;

public final class TestHelper {

	private static DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
			.scanPackage("org.eobjects.analyzer", true);

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration() {
		ConcurrencyProvider concurrencyProvider = new SingleThreadedConcurrencyProvider();
		CollectionProvider collectionProvider = new BerkeleyDbCollectionProvider();
		return new AnalyzerBeansConfigurationImpl(createDatastoreCatalog(),
				createReferenceDataCatalog(), descriptorProvider,
				concurrencyProvider, collectionProvider);
	}

	public static ReferenceDataCatalog createReferenceDataCatalog() {
		Collection<Dictionary> dictionaries = Collections.emptyList();
		Collection<SynonymCatalog> synonymCatalogs = Collections.emptyList();
		return new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs);
	}

	public static DatastoreCatalog createDatastoreCatalog() {
		List<Datastore> datastores = Collections.emptyList();
		return new DatastoreCatalogImpl(datastores);
	}
}
