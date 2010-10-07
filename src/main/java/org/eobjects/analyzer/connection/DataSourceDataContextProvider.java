package org.eobjects.analyzer.connection;

import javax.sql.DataSource;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class DataSourceDataContextProvider implements DataContextProvider {

	private final DataContext _dataContext;
	private final SchemaNavigator _schemaNavigator;
	private final Datastore _datastore;

	public DataSourceDataContextProvider(DataSource ds, Datastore datastore) {
		this._dataContext = DataContextFactory.createJdbcDataContext(ds);
		this._schemaNavigator = new SchemaNavigator(_dataContext);
		this._datastore = datastore;
	}

	@Override
	public DataContext getDataContext() {
		return _dataContext;
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		return _schemaNavigator;
	}

	@Override
	public Datastore getDatastore() {
		return _datastore;
	}

}
