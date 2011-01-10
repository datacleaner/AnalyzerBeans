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

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;

public final class JdbcDatastore extends UsageAwareDatastore {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(JdbcDatastore.class);

	private final String _name;
	private final String _jdbcUrl;
	private final String _username;
	private final String _password;
	private final String _driverClass;
	private final String _datasourceJndiUrl;

	private JdbcDatastore(String name, String jdbcUrl, String driverClass, String username, String password,
			String datasourceJndiUrl) {
		super();
		_name = name;
		_jdbcUrl = jdbcUrl;
		_driverClass = driverClass;
		_username = username;
		_password = password;
		_datasourceJndiUrl = datasourceJndiUrl;
	}

	public JdbcDatastore(String name, String jdbcUrl, String driverClass) {
		this(name, jdbcUrl, driverClass, null, null, null);
	}

	public JdbcDatastore(String name, String jdbcUrl, String driverClass, String username, String password) {
		this(name, jdbcUrl, driverClass, username, password, null);
	}

	public JdbcDatastore(String name, String datasourceJndiUrl) {
		this(name, null, null, null, null, datasourceJndiUrl);
	}

	/**
	 * Alternative constructor usable only for in-memory (ie. non-persistent)
	 * datastores, because the datastore will not be able to create new
	 * connections.
	 * 
	 * @param name
	 * @param dc
	 */
	public JdbcDatastore(String name, DataContext dc) {
		this(name, null, null, null, null, null);
		setDataContextProvider(new SingleDataContextProvider(dc, this));
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

	private Connection createConnection() {
		if (_jdbcUrl == null) {
			throw new IllegalStateException("JDBC URL is null, cannot create connection!");
		}

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
			} catch (Exception e) {
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
				return DriverManager.getConnection(_jdbcUrl);
			} else {
				return DriverManager.getConnection(_jdbcUrl, _username, _password);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Could not establish JDBC connection", e);
		}
	}

	@Override
	protected UsageAwareDataContextProvider createDataContextProvider() {
		if (StringUtils.isNullOrEmpty(_datasourceJndiUrl)) {
			Connection connection = createConnection();

			DataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			return new SingleDataContextProvider(dataContext, this, new CloseableJdbcConnection(connection));
		} else {
			try {
				InitialContext initialContext = new InitialContext();
				DataSource dataSource = (DataSource) initialContext.lookup(_datasourceJndiUrl);
				return new DataSourceDataContextProvider(dataSource, this);
			} catch (Exception e) {
				logger.error("Could not retrieve DataSource '{}'", _datasourceJndiUrl);
				throw new IllegalStateException(e);
			}
		}
	}
}
