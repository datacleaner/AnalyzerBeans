package org.eobjects.analyzer.job;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;

public class AnalyzerBeansConfigurationImpl implements
		AnalyzerBeansConfiguration {

	private static final long serialVersionUID = 1L;

	private DescriptorProvider _descriptorProvider;
	private CollectionProvider _collectionProvider;
	private DatastoreCatalog _datastoreCatalog;
	private ConcurrencyProvider _concurrencyProvider;

	public AnalyzerBeansConfigurationImpl(DatastoreCatalog datastoreCatalog,
			DescriptorProvider descriptorProvider,
			ConcurrencyProvider concurrencyProvider,
			CollectionProvider collectionProvider) {
		_datastoreCatalog = datastoreCatalog;
		_descriptorProvider = descriptorProvider;
		_concurrencyProvider = concurrencyProvider;
		_collectionProvider = collectionProvider;
	}

	@Override
	public DescriptorProvider getDescriptorProvider() {
		return _descriptorProvider;
	}

	@Override
	public CollectionProvider getCollectionProvider() {
		return _collectionProvider;
	}

	@Override
	public DatastoreCatalog getDatastoreCatalog() {
		return _datastoreCatalog;
	}

	@Override
	public ConcurrencyProvider getConcurrencyProvider() {
		return _concurrencyProvider;
	}

}
