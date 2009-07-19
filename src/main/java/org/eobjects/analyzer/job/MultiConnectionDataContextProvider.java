package org.eobjects.analyzer.job;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class MultiConnectionDataContextProvider implements DataContextProvider {

	private DataContext[] dataContexts;
	private String url;
	private String user;
	private String password;
	private String catalog;
	private int nextDataContext;
	private SchemaNavigator schemaNavigator;

	public MultiConnectionDataContextProvider(int maxConnections, String url) {
		this(maxConnections, url, null, null);
	}

	public MultiConnectionDataContextProvider(int maxConnections, String url,
			String user, String password) {
		this(maxConnections, url, user, password, null);
	}

	public MultiConnectionDataContextProvider(int maxConnections, String url,
			String user, String password, String catalog) {
		this.dataContexts = new DataContext[maxConnections];
		this.url = url;
		this.user = user;
		this.password = password;
		this.catalog = catalog;
		this.nextDataContext = 0;
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
		if (this.schemaNavigator == null) {
			// Always use the same datacontext for schema navigation purposes
			schemaNavigator = new SchemaNavigator(getDataContext(0));
		}
		return schemaNavigator;
	}

}
