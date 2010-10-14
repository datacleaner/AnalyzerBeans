package org.eobjects.analyzer.connection;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class AccessDatastore implements Datastore {

	private static final long serialVersionUID = 1L;
	private final String _name;
	private String _filename;

	public AccessDatastore(String name, String filename) {
		_name = name;
		_filename = filename;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		DataContext dc = DataContextFactory.createAccessDataContext(_filename);
		return new SingleDataContextProvider(dc, this);
	}

	@Override
	public void close() {
	}
}
