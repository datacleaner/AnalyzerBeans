package org.eobjects.analyzer.connection;

import java.io.File;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public final class ExcelDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final String _filename;

	public ExcelDatastore(String name, String filename) {
		_name = name;
		_filename = filename;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		DataContext dc = DataContextFactory.createExcelDataContext(new File(_filename));
		return new SingleDataContextProvider(dc, this);
	}

	@Override
	public void close() {
	}
}
