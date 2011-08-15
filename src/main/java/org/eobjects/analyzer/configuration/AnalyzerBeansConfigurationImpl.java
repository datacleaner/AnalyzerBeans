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
package org.eobjects.analyzer.configuration;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.storage.StorageProvider;

public final class AnalyzerBeansConfigurationImpl implements AnalyzerBeansConfiguration {

	private static final long serialVersionUID = 1L;

	private final transient DescriptorProvider _descriptorProvider;
	private final transient StorageProvider _storageProvider;
	private final transient TaskRunner _taskRunner;
	private final DatastoreCatalog _datastoreCatalog;
	private final ReferenceDataCatalog _referenceDataCatalog;
	private final InjectionManagerFactory _injectionManagerFactory;

	public AnalyzerBeansConfigurationImpl(DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
			DescriptorProvider descriptorProvider, TaskRunner taskRunner, StorageProvider storageProvider) {
		this(datastoreCatalog, referenceDataCatalog, descriptorProvider, taskRunner, storageProvider, null);
	}

	public AnalyzerBeansConfigurationImpl(DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
			DescriptorProvider descriptorProvider, TaskRunner taskRunner, StorageProvider storageProvider,
			InjectionManagerFactory injectionManagerFactory) {
		if (datastoreCatalog == null) {
			throw new IllegalArgumentException("datastoreCatalog cannot be null");
		}
		if (referenceDataCatalog == null) {
			throw new IllegalArgumentException("referenceDataCatalog cannot be null");
		}
		if (descriptorProvider == null) {
			throw new IllegalArgumentException("descriptorProvider cannot be null");
		}
		if (taskRunner == null) {
			throw new IllegalArgumentException("taskRunner cannot be null");
		}
		if (storageProvider == null) {
			throw new IllegalArgumentException("storageProvider cannot be null");
		}
		_datastoreCatalog = datastoreCatalog;
		_referenceDataCatalog = referenceDataCatalog;
		_descriptorProvider = descriptorProvider;
		_taskRunner = taskRunner;
		_storageProvider = storageProvider;

		if (injectionManagerFactory == null) {
			injectionManagerFactory = new InjectionManagerFactoryImpl(this);
		}
		_injectionManagerFactory = injectionManagerFactory;
	}
	
	@Override
	public InjectionManagerFactory getInjectionManagerFactory() {
		return _injectionManagerFactory;
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
	public StorageProvider getStorageProvider() {
		return _storageProvider;
	}

	@Override
	public TaskRunner getTaskRunner() {
		return _taskRunner;
	}

}
