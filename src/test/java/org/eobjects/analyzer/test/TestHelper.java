package org.eobjects.analyzer.test;

import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.job.ConcurrencyProvider;
import org.eobjects.analyzer.job.SingleThreadedConcurrencyProvider;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;

public final class TestHelper {

	private static DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
			.scanPackage("org.eobjects.analyzer", true);

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration() {
		List<Datastore> emptyList = Collections.emptyList();
		DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(emptyList);
		ConcurrencyProvider concurrencyProvider = new SingleThreadedConcurrencyProvider();
		CollectionProvider collectionProvider = new BerkeleyDbCollectionProvider();
		return new AnalyzerBeansConfigurationImpl(datastoreCatalog,
				descriptorProvider, concurrencyProvider, collectionProvider);
	}
}
