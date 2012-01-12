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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.UpdateableDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datastore implementation for JDBC based connections. Connections can either
 * be based on JDBC urls or JNDI urls.
 * 
 * @author Kasper Sørensen
 * 
 */
public class JdbcDatastore extends UsageAwareDatastore<UpdateableDataContext> implements UpdateableDatastore {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(JdbcDatastore.class);

	private final String _jdbcUrl;
	private final String _username;
	private final String _password;
	private final String _driverClass;
	private final boolean _multipleConnections;
	private final String _datasourceJndiUrl;

	private JdbcDatastore(String name, String jdbcUrl, String driverClass, String username, String password,
			String datasourceJndiUrl, boolean multipleConnections) {
		super(name);
		_jdbcUrl = jdbcUrl;
		_driverClass = driverClass;
		_username = username;
		_password = password;
		_datasourceJndiUrl = datasourceJndiUrl;
		_multipleConnections = multipleConnections;
	}

	public JdbcDatastore(String name, String jdbcUrl, String driverClass) {
		this(name, jdbcUrl, driverClass, null, null, null, true);
	}

	public JdbcDatastore(String name, String jdbcUrl, String driverClass, String username, String password,
			boolean multipleConnections) {
		this(name, jdbcUrl, driverClass, username, password, null, multipleConnections);
	}

	public JdbcDatastore(String name, String datasourceJndiUrl) {
		this(name, null, null, null, null, datasourceJndiUrl, false);
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, JdbcDatastore.class).readObject(stream);
	}

	@Override
	public UpdateableDatastoreConnection openConnection() {
		DatastoreConnection connection = super.openConnection();
		return (UpdateableDatastoreConnection) connection;
	}

	/**
	 * Alternative constructor usable only for in-memory (ie. non-persistent)
	 * datastores, because the datastore will not be able to create new
	 * connections.
	 * 
	 * @param name
	 * @param dc
	 */
	public JdbcDatastore(String name, UpdateableDataContext dc) {
		this(name, null, null, null, null, null, false);
		setDataContextProvider(new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dc, this));
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_driverClass);
		identifiers.add(_jdbcUrl);
		identifiers.add(_datasourceJndiUrl);
		identifiers.add(_username);
		identifiers.add(_password);
		identifiers.add(_multipleConnections);
	}

	public boolean isMultipleConnections() {
		return _multipleConnections;
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

	public String getDatasourceJndiUrl() {
		return _datasourceJndiUrl;
	}

	public Connection createConnection() throws IllegalStateException {
		initializeDriver();

		try {
			if (_username != null && _password != null) {
				return DriverManager.getConnection(_jdbcUrl, _username, _password);
			} else {
				return DriverManager.getConnection(_jdbcUrl);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Could not create connection", e);
		}
	}

	public DataSource createDataSource() {
		initializeDriver();

		BasicDataSource ds = new BasicDataSource();
		ds.setMaxActive(-1);
		ds.setDefaultAutoCommit(false);
		ds.setUrl(_jdbcUrl);

		if (_username != null && _password != null) {
			ds.setUsername(_username);
			ds.setPassword(_password);
		}
		return ds;
	}

	private void initializeDriver() {
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
	}

	@Override
	protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
		if (StringUtils.isNullOrEmpty(_datasourceJndiUrl)) {
			if (isMultipleConnections()) {
				final DataSource dataSource = createDataSource();
				return new DataSourceDatastoreConnection(dataSource, this);
			} else {
				final Connection connection = createConnection();
				final UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
				return new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dataContext, this);
			}
		} else {
			try {
				Context initialContext = getJndiNamingContext();
				DataSource dataSource = (DataSource) initialContext.lookup(_datasourceJndiUrl);
				return new DataSourceDatastoreConnection(dataSource, this);
			} catch (Exception e) {
				logger.error("Could not retrieve DataSource '{}'", _datasourceJndiUrl);
				throw new IllegalStateException(e);
			}
		}
	}

	protected Context getJndiNamingContext() throws NamingException {
		return new InitialContext();
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(true);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("JdbcDatastore[name=");
		sb.append(getName());
		if (_jdbcUrl != null) {
			sb.append(",url=");
			sb.append(_jdbcUrl);
		} else {
			sb.append(",jndi=");
			sb.append(_datasourceJndiUrl);
		}
		sb.append("]");
		return sb.toString();
	}
}
