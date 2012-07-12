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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.csv.CsvConfiguration;
import org.eobjects.metamodel.csv.CsvDataContext;
import org.eobjects.metamodel.util.FileHelper;

/**
 * Datastore implementation for CSV files.
 * 
 * @author Kasper Sørensen
 */
public final class CsvDatastore extends UsageAwareDatastore<UpdateableDataContext> implements FileDatastore,
        UpdateableDatastore {

    private static final long serialVersionUID = 1L;

    /**
     * The value is '\\uFFFF', the "not a character" value which should not
     * occur in any valid Unicode string.
     */
    public static final char NOT_A_CHAR = '\uFFFF';

    public static final char DEFAULT_QUOTE_CHAR = NOT_A_CHAR;
    public static final char DEFAULT_SEPARATOR_CHAR = CsvConfiguration.DEFAULT_SEPARATOR_CHAR;

    private final String _filename;
    private final Character _quoteChar;
    private final Character _separatorChar;
    private final Character _escapeChar;
    private final String _encoding;
    private final boolean _failOnInconsistencies;
    private final int _headerLineNumber;

    public CsvDatastore(String name, String filename) {
        this(name, filename, null, null, null);
    }

    public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar, String encoding) {
        this(name, filename, quoteChar, separatorChar, encoding, true);
    }

    public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar, String encoding,
            boolean failOnInconsistencies) {
        this(name, filename, quoteChar, separatorChar, encoding, failOnInconsistencies,
                CsvConfiguration.DEFAULT_COLUMN_NAME_LINE);
    }

    public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar, String encoding,
            boolean failOnInconsistencies, int headerLineNumber) {
        this(name, filename, quoteChar, separatorChar, CsvConfiguration.DEFAULT_ESCAPE_CHAR, encoding,
                failOnInconsistencies, headerLineNumber);
    }

    public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar,
            Character escapeChar, String encoding, boolean failOnInconsistencies, int headerLineNumber) {
        super(name);
        _filename = filename;
        _quoteChar = quoteChar;
        _separatorChar = separatorChar;
        _escapeChar = escapeChar;
        _encoding = encoding;
        _failOnInconsistencies = failOnInconsistencies;
        if (headerLineNumber < 0) {
            headerLineNumber = CsvConfiguration.NO_COLUMN_NAME_LINE;
        }
        _headerLineNumber = headerLineNumber;

    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, CsvDatastore.class).readObject(stream);
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

    public Character getEscapeChar() {
        return _escapeChar;
    }

    public Character getSeparatorChar() {
        return _separatorChar;
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        final char separatorChar = _separatorChar == null ? DEFAULT_SEPARATOR_CHAR : _separatorChar;
        final char quoteChar = _quoteChar == null ? DEFAULT_QUOTE_CHAR : _quoteChar;
        final char escapeChar = _escapeChar == null ? CsvConfiguration.DEFAULT_ESCAPE_CHAR : _escapeChar;
        final String encoding = _encoding == null ? FileHelper.UTF_8_ENCODING : _encoding;
        final CsvConfiguration configuration = new CsvConfiguration(_headerLineNumber, encoding, separatorChar,
                quoteChar, escapeChar, _failOnInconsistencies);
        final UpdateableDataContext dataContext = new CsvDataContext(new File(_filename), configuration);
        return new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dataContext, this);
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false);
    }

    public boolean isFailOnInconsistencies() {
        return _failOnInconsistencies;
    }

    public int getHeaderLineNumber() {
        return _headerLineNumber;
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_filename);
        identifiers.add(_encoding);
        identifiers.add(_quoteChar);
        identifiers.add(_escapeChar);
        identifiers.add(_separatorChar);
        identifiers.add(_failOnInconsistencies);
        identifiers.add(_headerLineNumber);
    }

    @Override
    public String toString() {
        return "CsvDatastore[name=" + getName() + ", filename=" + _filename + ", quoteChar='" + _quoteChar
                + "', separatorChar='" + _separatorChar + "', encoding=" + _encoding + ", headerLineNumber="
                + _headerLineNumber + "]";
    }
}
