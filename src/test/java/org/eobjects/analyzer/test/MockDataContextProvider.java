package org.eobjects.analyzer.test;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public final class MockDataContextProvider implements DataContextProvider {

	@Override
	public DataContext getDataContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Datastore getDatastore() {
		throw new UnsupportedOperationException();
	}

}
