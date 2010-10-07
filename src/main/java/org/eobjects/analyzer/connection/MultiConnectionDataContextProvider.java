package org.eobjects.analyzer.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public final class MultiConnectionDataContextProvider implements DataContextProvider {

	private final DataContext[] dataContexts;
	private final String url;
	private final String user;
	private final String password;
	private final String catalog;
	private final Datastore datastore;
	private SchemaNavigator schemaNavigator;
	private int nextDataContext;

	public MultiConnectionDataContextProvider(int maxConnections, String url, Datastore datastore) {
		this(maxConnections, url, null, null, datastore);
	}

	public MultiConnectionDataContextProvider(int maxConnections, String url, String user, String password,
			Datastore datastore) {
		this(maxConnections, url, user, password, null, datastore);
	}

	public MultiConnectionDataContextProvider(int maxConnections, String url, String user, String password, String catalog,
			Datastore datastore) {
		this.dataContexts = new DataContext[maxConnections];
		this.url = url;
		this.user = user;
		this.password = password;
		this.catalog = catalog;
		this.nextDataContext = 0;
		this.datastore = datastore;
	}

	@Override
	public DataContext getDataContext() {
		return getNextDataContext();
	}

	private DataContext getNextDataContext() {
		if (nextDataContext == dataContexts.length) {
			nextDataContext = 0;
		}
		DataContext dc = getDataContext(nextDataContext);
		nextDataContext++;
		return dc;
	}

	private DataContext getDataContext(int index) {
		DataContext dataContext = dataContexts[index];
		if (dataContext == null) {
			try {
				Connection con;
				if (user == null && password == null) {
					con = DriverManager.getConnection(url);
				} else {
					con = DriverManager.getConnection(url, user, password);
				}
				if (catalog != null) {
					con.setCatalog(catalog);
				}
				dataContext = DataContextFactory.createJdbcDataContext(con);
				dataContexts[index] = dataContext;
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
		return dataContext;
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		if (schemaNavigator == null) {
			synchronized (this) {
				if (schemaNavigator == null) {
					// Always use the same datacontext for schema navigation
					// purposes
					schemaNavigator = new SchemaNavigator(getDataContext(0));
				}
			}
		}
		return schemaNavigator;
	}

	@Override
	public Datastore getDatastore() {
		return datastore;
	}

}
