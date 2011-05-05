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
package org.eobjects.analyzer.connection;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eobjects.metamodel.util.BaseObject;

/**
 * Abstract datastore implementation that uses a shared
 * UsageAwareDataContextProvider when posssible.
 * 
 * @see UsageAwareDataContextProvider
 * 
 * @author Kasper Sørensen
 */
public abstract class UsageAwareDatastore extends BaseObject implements Datastore {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(UsageAwareDatastore.class);

	private transient volatile Reference<UsageAwareDataContextProvider> _dataContextProviderRef;
	private transient volatile DataContextProvider _dataContextProvider = null;

	protected Reference<UsageAwareDataContextProvider> getDataContextProviderRef() {
		return _dataContextProviderRef;
	}

	protected void setDataContextProviderRef(Reference<UsageAwareDataContextProvider> dataContextProviderRef) {
		_dataContextProviderRef = dataContextProviderRef;
	}

	protected void setDataContextProvider(DataContextProvider dataContextProvider) {
		_dataContextProvider = dataContextProvider;
	}

	@Override
	public synchronized final DataContextProvider getDataContextProvider() {
		if (_dataContextProvider != null) {
			return _dataContextProvider;
		}

		UsageAwareDataContextProvider dataContextProvider;
		if (_dataContextProviderRef != null) {
			dataContextProvider = _dataContextProviderRef.get();
			if (isDataContextProviderOpen(dataContextProvider)) {
				// reuse existing data context provider
				logger.info("Reusing existing DataContextProvider: {}", dataContextProvider);
				dataContextProvider.incrementUsageCount();
				return dataContextProvider;
			}
		}

		dataContextProvider = createDataContextProvider();
		if (dataContextProvider == null) {
			throw new IllegalStateException("createDataContextProvider() returned null");
		}
		_dataContextProviderRef = new WeakReference<UsageAwareDataContextProvider>(dataContextProvider);

		return dataContextProvider;
	}

	protected abstract UsageAwareDataContextProvider createDataContextProvider();

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(getName());
	}

	/**
	 * Gets whether the datacontext provider is already open / ready or if it
	 * has to be created.
	 * 
	 * @return a boolean indicating if the datacontext provider is open
	 */
	public boolean isDataContextProviderOpen() {
		if (_dataContextProviderRef == null) {
			return false;
		}
		UsageAwareDataContextProvider dataContextProvider = _dataContextProviderRef.get();
		return isDataContextProviderOpen(dataContextProvider);
	}

	private boolean isDataContextProviderOpen(UsageAwareDataContextProvider dataContextProvider) {
		return dataContextProvider != null && !dataContextProvider.isClosed();
	}
}
