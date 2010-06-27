package org.eobjects.analyzer.job;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.concurrent.ConcurrencyProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;

public class AnalyzerBeansConfigurationImpl implements
		AnalyzerBeansConfiguration {

	private static final long serialVersionUID = 1L;

	private DescriptorProvider _descriptorProvider;
	private CollectionProvider _collectionProvider;
	private DatastoreCatalog _datastoreCatalog;
	private ConcurrencyProvider _concurrencyProvider;

	private ReferenceDataCatalog _referenceDataCatalog;

	public AnalyzerBeansConfigurationImpl(DatastoreCatalog datastoreCatalog,
			ReferenceDataCatalog referenceDataCatalog,
			DescriptorProvider descriptorProvider,
			ConcurrencyProvider concurrencyProvider,
			CollectionProvider collectionProvider) {
		if (datastoreCatalog == null) {
			throw new IllegalArgumentException(
					"datastoreCatalog cannot be null");
		}
		if (referenceDataCatalog == null) {
			throw new IllegalArgumentException(
					"referenceDataCatalog cannot be null");
		}
		if (descriptorProvider == null) {
			throw new IllegalArgumentException(
					"descriptorProvider cannot be null");
		}
		if (concurrencyProvider == null) {
			throw new IllegalArgumentException(
					"concurrencyProvider cannot be null");
		}
		if (collectionProvider == null) {
			throw new IllegalArgumentException(
					"collectionProvider cannot be null");
		}
		_datastoreCatalog = datastoreCatalog;
		_referenceDataCatalog = referenceDataCatalog;
		_descriptorProvider = descriptorProvider;
		_concurrencyProvider = concurrencyProvider;
		_collectionProvider = collectionProvider;
	}

	@Override
	public DatastoreCatalog getDatastoreCatalog() {
		return _datastoreCatalog;
	}

	@Override
	public ReferenceDataCatalog getReferenceDataCatalog() {
		return _referenceDataCatalog;
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
	public ConcurrencyProvider getConcurrencyProvider() {
		return _concurrencyProvider;
	}

}
