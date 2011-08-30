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
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.storage.StorageProvider;

public class InjectionManagerFactoryImpl implements InjectionManagerFactory {

	private final DatastoreCatalog _datastoreCatalog;
	private final ReferenceDataCatalog _referenceDataCatalog;
	private final StorageProvider _storageProvider;

	public InjectionManagerFactoryImpl(AnalyzerBeansConfiguration configuration) {
		this(configuration.getDatastoreCatalog(), configuration.getReferenceDataCatalog(), configuration
				.getStorageProvider());
	}

	public InjectionManagerFactoryImpl(DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
			StorageProvider storageProvider) {
		_datastoreCatalog = datastoreCatalog;
		_referenceDataCatalog = referenceDataCatalog;
		_storageProvider = storageProvider;
	}

	@Override
	public InjectionManager getInjectionManager(AnalysisJob job) {
		return new InjectionManagerImpl(_datastoreCatalog, _referenceDataCatalog, _storageProvider, job);
	}
}
