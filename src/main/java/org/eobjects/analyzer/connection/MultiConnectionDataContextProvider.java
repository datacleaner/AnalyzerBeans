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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eobjects.analyzer.util.SchemaNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public final class MultiConnectionDataContextProvider extends UsageAwareDataContextProvider {

	private static final Logger logger = LoggerFactory.getLogger(MultiConnectionDataContextProvider.class);

	private final DataContext[] _dataContexts;
	private final Connection[] _connections;
	private final String _url;
	private final String _user;
	private final String _password;
	private final String _catalog;
	private volatile SchemaNavigator _schemaNavigator;
	private int _nextDataContext;

	public MultiConnectionDataContextProvider(int maxConnections, String url, Datastore datastore) {
		this(maxConnections, url, null, null, datastore);
	}

	public MultiConnectionDataContextProvider(int maxConnections, String url, String user, String password,
			Datastore datastore) {
		this(maxConnections, url, user, password, null, datastore);
	}

	public MultiConnectionDataContextProvider(int maxConnections, String url, String user, String password, String catalog,
			Datastore datastore) {
		super(datastore);
		_dataContexts = new DataContext[maxConnections];
		_connections = new Connection[maxConnections];
		_url = url;
		_user = user;
		_password = password;
		_catalog = catalog;
		_nextDataContext = 0;
	}

	@Override
	public DataContext getDataContext() {
		return getNextDataContext();
	}

	private DataContext getNextDataContext() {
		if (_nextDataContext == _dataContexts.length) {
			_nextDataContext = 0;
		}
		DataContext dc = getDataContext(_nextDataContext);
		_nextDataContext++;
		return dc;
	}

	private DataContext getDataContext(int index) {
		DataContext dataContext = _dataContexts[index];
		if (dataContext == null) {
			try {
				Connection con;
				if (_user == null && _password == null) {
					con = DriverManager.getConnection(_url);
				} else {
					con = DriverManager.getConnection(_url, _user, _password);
				}
				if (_catalog != null) {
					con.setCatalog(_catalog);
				}
				dataContext = DataContextFactory.createJdbcDataContext(con);
				_connections[index] = con;
				_dataContexts[index] = dataContext;
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
		return dataContext;
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		if (_schemaNavigator == null) {
			synchronized (this) {
				if (_schemaNavigator == null) {
					// Always use the same datacontext for schema navigation
					// purposes
					_schemaNavigator = new SchemaNavigator(getDataContext(0));
				}
			}
		}
		return _schemaNavigator;
	}

	@Override
	protected void closeInternal() {
		for (int i = 0; i < _connections.length; i++) {
			Connection con = _connections[i];
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					logger.error("Could not close _connections[" + i + "]", e);
				}
			}
		}
	}

}
