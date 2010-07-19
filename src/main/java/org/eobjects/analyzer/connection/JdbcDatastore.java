package org.eobjects.analyzer.connection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class JdbcDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private String _name;
	private String _url;
	private String _username;
	private String _password;
	private String _driverClass;
	private transient DataContextProvider _dataContextProvider;
	private transient Connection _connection;

	public JdbcDatastore(String name, String url, String driverClass) {
		_name = name;
		_url = url;
		_driverClass = driverClass;
	}

	public JdbcDatastore(String name, String url, String driverClass,
			String username, String password) {
		this(name, url, driverClass);
		_username = username;
		_password = password;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String url) {
		_url = url;
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		synchronized (this) {
			_username = username;
		}
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		synchronized (this) {
			_password = password;
		}
	}

	public String getDriverClass() {
		return _driverClass;
	}

	public void setDriverClass(String driverClass) {
		_driverClass = driverClass;
	}

	@Override
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		if (_dataContextProvider == null) {
			synchronized (this) {
				if (_dataContextProvider == null) {
					try {
						Class.forName(_driverClass);
					} catch (ClassNotFoundException e) {
						throw new IllegalStateException(
								"Could not initialize JDBC driver", e);
					}
					try {
						if (_username == null && _password == null) {
							_connection = DriverManager.getConnection(_url);
						} else {
							_connection = DriverManager.getConnection(_url,
									_username, _password);
						}
					} catch (SQLException e) {
						throw new IllegalStateException(
								"Could not establish JDBC connection", e);
					}

					DataContext dataContext = DataContextFactory
							.createJdbcDataContext(_connection);
					_dataContextProvider = new SingleDataContextProvider(
							dataContext);
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
