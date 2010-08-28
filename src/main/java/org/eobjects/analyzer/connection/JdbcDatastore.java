package org.eobjects.analyzer.connection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.eobjects.analyzer.util.StringUtils;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class JdbcDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private String _name;
	private String _jdbcUrl;
	private String _username;
	private String _password;
	private String _driverClass;
	private String _datasourceJndiUrl;
	private transient DataContextProvider _dataContextProvider;
	private transient Connection _connection;

	public JdbcDatastore(String name, String jdbcUrl, String driverClass) {
		_name = name;
		_jdbcUrl = jdbcUrl;
		_driverClass = driverClass;
	}

	public JdbcDatastore(String name, String url, String driverClass,
			String username, String password) {
		this(name, url, driverClass);
		_username = username;
		_password = password;
	}

	public JdbcDatastore(String name, String datasourceJndiUrl) {
		_name = name;
		_datasourceJndiUrl = datasourceJndiUrl;

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
						try {
							Class.forName(_driverClass);
						} catch (ClassNotFoundException e) {
							throw new IllegalStateException(
									"Could not initialize JDBC driver", e);
						}
						try {
							if (_username == null && _password == null) {
								_connection = DriverManager
										.getConnection(_jdbcUrl);
							} else {
								_connection = DriverManager.getConnection(
										_jdbcUrl, _username, _password);
							}
						} catch (SQLException e) {
							throw new IllegalStateException(
									"Could not establish JDBC connection", e);
						}

						DataContext dataContext = DataContextFactory
								.createJdbcDataContext(_connection);
						_dataContextProvider = new SingleDataContextProvider(
								dataContext);
					} else {
						try {
							InitialContext initialContext = new InitialContext();
							DataSource dataSource = (DataSource) initialContext
									.lookup(_datasourceJndiUrl);
							_dataContextProvider = new DataSourceDataContextProvider(
									dataSource);
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
				}
			}
		}
		return _dataContextProvider;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	@Override
	public void close() throws IOException {
		if (_connection != null) {
			try {
				_connection.close();
			} catch (SQLException e) {
				// do nothing
			}
		}
	}
}
