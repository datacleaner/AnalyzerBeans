package org.eobjects.analyzer.connection;

import java.io.File;

import dk.eobjects.metamodel.CsvDataContextStrategy;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.DefaultDataContext;
import dk.eobjects.metamodel.util.FileHelper;

public final class CsvDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final String _filename;
	private final Character _quoteChar;
	private final Character _separatorChar;
	private final String _encoding;

	public CsvDatastore(String name, String filename) {
		this(name, filename, null, null, null);
	}

	public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar, String encoding) {
		_name = name;
		_filename = filename;
		_quoteChar = quoteChar;
		_separatorChar = separatorChar;
		_encoding = encoding;
	}

	@Override
	public void close() {
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		DataContext dataContext;
		if (_quoteChar == null && _separatorChar == null) {
			dataContext = DataContextFactory.createCsvDataContext(new File(_filename));
		} else {
			char separatorChar = _separatorChar == null ? DataContextFactory.DEFAULT_CSV_SEPARATOR_CHAR : _separatorChar;
			char quoteChar = _quoteChar == null ? DataContextFactory.DEFAULT_CSV_QUOTE_CHAR : _quoteChar;
			dataContext = new DefaultDataContext(new CsvDataContextStrategy(new File(_filename), separatorChar, quoteChar,
					(_encoding == null ? FileHelper.UTF_8_ENCODING : _encoding)));
		}
		return new SingleDataContextProvider(dataContext, this);
	}

}
