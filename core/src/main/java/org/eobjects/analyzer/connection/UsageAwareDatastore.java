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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.eobjects.analyzer.util.ReadObjectBuilder.Moved;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.util.BaseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract datastore implementation that uses a shared
 * UsageAwareDataContextProvider when posssible.
 * 
 * @see UsageAwareDatastoreConnection
 * 
 * @author Kasper Sørensen
 */
public abstract class UsageAwareDatastore<E extends DataContext> extends BaseObject implements Datastore {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(UsageAwareDatastore.class);

	private transient volatile Reference<UsageAwareDatastoreConnection<E>> _datastoreConnectionRef;
	private transient volatile UsageAwareDatastoreConnection<E> _dataContextProvider = null;

	@Moved
	private final String _name;

	private String _description;

	public UsageAwareDatastore(String name) {
		_name = name;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, UsageAwareDatastore.class).readObject(stream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		return _name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getDescription() {
		return _description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDescription(String description) {
		_description = description;
	}

	protected Reference<UsageAwareDatastoreConnection<E>> getDataContextProviderRef() {
		return _datastoreConnectionRef;
	}

	protected void setDataContextProviderRef(Reference<UsageAwareDatastoreConnection<E>> dataContextProviderRef) {
		_datastoreConnectionRef = dataContextProviderRef;
	}

	protected void setDataContextProvider(UsageAwareDatastoreConnection<E> dataContextProvider) {
		_dataContextProvider = dataContextProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized final UsageAwareDatastoreConnection<E> getDataContextProvider() {
		if (_dataContextProvider != null) {
			return _dataContextProvider;
		}

		UsageAwareDatastoreConnection<E> datastoreConnection;
		if (_datastoreConnectionRef != null) {
			datastoreConnection = _datastoreConnectionRef.get();
			if (datastoreConnection != null && datastoreConnection.requestUsage()) {
				// reuse existing data context provider
				logger.info("Reusing existing DatastoreConnection: {}", datastoreConnection);
				return datastoreConnection;
			}
		}

		datastoreConnection = createDatastoreConnection();
		if (datastoreConnection == null) {
			throw new IllegalStateException("createDatastoreConnection() returned null");
		}
		_datastoreConnectionRef = new WeakReference<UsageAwareDatastoreConnection<E>>(datastoreConnection);

		return datastoreConnection;
	}

	@Override
	public DatastoreConnection openConnection() {
		return getDataContextProvider();
	}

	protected abstract UsageAwareDatastoreConnection<E> createDatastoreConnection();

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_name);
		identifiers.add(_description);
	}

	/**
	 * Gets whether the datacontext provider is already open / ready or if it
	 * has to be created.
	 * 
	 * @return a boolean indicating if the datacontext provider is open
	 */
	public final boolean isDatastoreConnectionOpen() {
		if (_datastoreConnectionRef == null) {
			return false;
		}
		UsageAwareDatastoreConnection<E> dataContextProvider = _datastoreConnectionRef.get();
		return isDataContextProviderOpen(dataContextProvider);
	}

	private boolean isDataContextProviderOpen(UsageAwareDatastoreConnection<E> dataContextProvider) {
		return dataContextProvider != null && !dataContextProvider.isClosed();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + getName() + "]";
	}
}
