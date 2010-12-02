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
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.eobjects.analyzer.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public final class JdbcDatastore implements Datastore {

	private static final Logger logger = LoggerFactory.getLogger(JdbcDatastore.class);
	private static final long serialVersionUID = 1L;

	private String _name;
	private String _jdbcUrl;
	private String _username;
	private String _password;
	private String _driverClass;
	private String _datasourceJndiUrl;
	private transient volatile DataContextProvider _dataContextProvider;
	private transient Connection _connection;

	public JdbcDatastore(String name, String jdbcUrl, String driverClass) {
		_name = name;
		_jdbcUrl = jdbcUrl;
		_driverClass = driverClass;
	}

	public JdbcDatastore(String name, String url, String driverClass, String username, String password) {
		this(name, url, driverClass);
		_username = username;
		_password = password;
	}

	public JdbcDatastore(String name, String datasourceJndiUrl) {
		_name = name;
		_datasourceJndiUrl = datasourceJndiUrl;

	}

	public JdbcDatastore(String name, DataContext dc) {
		_name = name;
		_dataContextProvider = new SingleDataContextProvider(dc, this);
	}

	public String getJdbcUrl() {
		return _jdbcUrl;
	}

	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}

	public String getDriverClass() {
		return _driverClass;
	}

	@Override
	public String getName() {
		return _name;
	}

	public String getDatasourceJndiUrl() {
		return _datasourceJndiUrl;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		if (_dataContextProvider == null) {
			synchronized (this) {
				if (_dataContextProvider == null) {
					if (StringUtils.isNullOrEmpty(_datasourceJndiUrl)) {

						_connection = getConnection();

						DataContext dataContext = DataContextFactory.createJdbcDataContext(_connection);
						_dataContextProvider = new SingleDataContextProvider(dataContext, this);
					} else {
						try {
							InitialContext initialContext = new InitialContext();
							DataSource dataSource = (DataSource) initialContext.lookup(_datasourceJndiUrl);
							_dataContextProvider = new DataSourceDataContextProvider(dataSource, this);
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
				}
			}
		}
		return _dataContextProvider;
	}

	public Connection getConnection() {
		if (_connection == null) {

			logger.debug("Determining if driver initialization is nescesary");

			// it's best to avoid initializing the driver, so we do this check.
			// It may already have been initialized and Class.forName(...) does
			// not always work if the driver is in a different classloader
			boolean installDriver = true;

			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements()) {
				Driver driver = drivers.nextElement();
				try {
					if (driver.acceptsURL(_jdbcUrl)) {
						installDriver = false;
						break;
					}
				} catch (SQLException e) {
					logger.warn("Driver threw exception when acceptURL(...) was invoked", e);
				}
			}

			if (installDriver) {
				try {
					Class.forName(_driverClass);
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Could not initialize JDBC driver", e);
				}
			}

			try {
				if (_username == null && _password == null) {
					_connection = DriverManager.getConnection(_jdbcUrl);
				} else {
					_connection = DriverManager.getConnection(_jdbcUrl, _username, _password);
				}
			} catch (SQLException e) {
				throw new IllegalStateException("Could not establish JDBC connection", e);
			}
		}
		return _connection;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	@Override
	public void close() {
		if (_connection != null) {
			try {
				_connection.close();
			} catch (SQLException e) {
				// do nothing
			}
			_connection = null;
		}
	}
}
