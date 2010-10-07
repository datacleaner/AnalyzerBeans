package org.eobjects.analyzer.connection;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public interface DataContextProvider {

	public DataContext getDataContext();

	public SchemaNavigator getSchemaNavigator();

	public Datastore getDatastore();
}
