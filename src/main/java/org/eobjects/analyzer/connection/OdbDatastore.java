package org.eobjects.analyzer.connection;

import java.io.File;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

/**
 * Datastore implementation for OpenOffice database files (.odb).
 * 
 * @author Kasper Sørensen
 */
public class OdbDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final String _filename;
	private transient DataContextProvider _dataContextProvider;

	public OdbDatastore(String name, String filename) {
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
			DataContext dc = DataContextFactory.createOpenOfficeDataContext(new File(_filename));
			_dataContextProvider = new SingleDataContextProvider(dc, this);
		}
		return _dataContextProvider;
	}

	@Override
	public void close() {
		_dataContextProvider = null;
	}

}
