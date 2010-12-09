package org.eobjects.analyzer.test;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;

public class MockDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "MockDatastore";
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		return new MockDataContextProvider();
	}
}
