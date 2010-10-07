package org.eobjects.analyzer.connection;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public final class SingleDataContextProvider implements DataContextProvider {

	private final DataContext dataContext;
	private final SchemaNavigator schemaNavigator;
	private final Datastore datastore;

	public SingleDataContextProvider(DataContext dataContext, Datastore datastore) {
		this.dataContext = dataContext;
		this.schemaNavigator = new SchemaNavigator(dataContext);
		this.datastore = datastore;
	}

	@Override
	public DataContext getDataContext() {
		return this.dataContext;
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		return this.schemaNavigator;
	}

	@Override
	public Datastore getDatastore() {
		return datastore;
	}
}
