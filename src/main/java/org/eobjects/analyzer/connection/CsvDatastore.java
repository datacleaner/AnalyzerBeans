/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.connection;

import java.io.File;

import org.eobjects.metamodel.CsvDataContextStrategy;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.DefaultDataContext;
import org.eobjects.metamodel.util.FileHelper;

public final class CsvDatastore extends UsageAwareDatastore implements FileDatastore {

	private static final long serialVersionUID = 1L;

	/**
	 * The value is '\\uFFFF', the "not a character" value which should not
	 * occur in any valid Unicode string.
	 */
	public static final char NOT_A_CHAR = '\uFFFF';
	
	public static final char DEFAULT_QUOTE_CHAR = NOT_A_CHAR;
	public static final char DEFAULT_SEPARATOR_CHAR = DataContextFactory.DEFAULT_CSV_SEPARATOR_CHAR;

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
	public String getName() {
		return _name;
	}

	public String getEncoding() {
		return _encoding;
	}

	@Override
	public String getFilename() {
		return _filename;
	}

	public Character getQuoteChar() {
		return _quoteChar;
	}

	public Character getSeparatorChar() {
		return _separatorChar;
	}

	@Override
	protected UsageAwareDataContextProvider createDataContextProvider() {
		DataContext dataContext;
		if (_quoteChar == null && _separatorChar == null) {
			dataContext = DataContextFactory.createCsvDataContext(new File(_filename));
		} else {
			char separatorChar = _separatorChar == null ? DEFAULT_SEPARATOR_CHAR : _separatorChar;
			char quoteChar = _quoteChar == null ? DEFAULT_QUOTE_CHAR : _quoteChar;
			dataContext = new DefaultDataContext(new CsvDataContextStrategy(new File(_filename), separatorChar, quoteChar,
					(_encoding == null ? FileHelper.UTF_8_ENCODING : _encoding)));
		}
		return new SingleDataContextProvider(dataContext, this);
	}
	
	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(false);
	}

}
