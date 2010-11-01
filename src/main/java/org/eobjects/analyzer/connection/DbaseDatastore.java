package org.eobjects.analyzer.connection;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class DbaseDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final String _filename;
	private transient DataContextProvider _dataContextProvider;

	public DbaseDatastore(String name, String filename) {
		_name = name;
		_filename = filename;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		if (_dataContextProvider == null) {
			DataContext dc = DataContextFactory.createDbaseDataContext(_filename);
			_dataContextProvider = new SingleDataContextProvider(dc, this);
		}
		return _dataContextProvider;
	}

	@Override
	public void close() {
		_dataContextProvider = null;
	}

}
