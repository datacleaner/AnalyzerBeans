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
import java.util.Arrays;
import java.util.List;

import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.fixedwidth.FixedWidthConfiguration;

/**
 * Datastore based on fixed width files
 * 
 * @author Kasper Sørensen
 * 
 */
public class FixedWidthDatastore extends UsageAwareDatastore<DataContext> implements FileDatastore {

	private static final long serialVersionUID = 1L;

	private final String _filename;
	private final String _encoding;
	private final int _fixedValueWidth;
	private final int[] _valueWidths;
	private final boolean _failOnInconsistencies;

	public FixedWidthDatastore(String name, String filename, String encoding, int fixedValueWidth) {
		this(name, filename, encoding, fixedValueWidth, true);
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int[] valueWidths) {
		this(name, filename, encoding, valueWidths, true);
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int fixedValueWidth,
			boolean failOnInconsistencies) {
		super(name);
		_filename = filename;
		_encoding = encoding;
		_fixedValueWidth = fixedValueWidth;
		_valueWidths = new int[0];
		_failOnInconsistencies = failOnInconsistencies;
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int[] valueWidths,
			boolean failOnInconsistencies) {
		super(name);
		_filename = filename;
		_encoding = encoding;
		_fixedValueWidth = -1;
		_valueWidths = valueWidths;
		_failOnInconsistencies = failOnInconsistencies;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, FixedWidthDatastore.class).readObject(stream);
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(false);
	}

	@Override
	protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
		File file = new File(_filename);
		assert file.exists();

		final int columnNameLineNumber = 0;
		final FixedWidthConfiguration configuration;
		if (_fixedValueWidth == -1) {
			configuration = new FixedWidthConfiguration(columnNameLineNumber, _encoding, _valueWidths,
					_failOnInconsistencies);
		} else {
			configuration = new FixedWidthConfiguration(columnNameLineNumber, _encoding, _fixedValueWidth,
					_failOnInconsistencies);
		}

		DataContext dataContext = DataContextFactory.createFixedWidthDataContext(file, configuration);
		return new DatastoreConnectionImpl<DataContext>(dataContext, this);
	}

	public String getEncoding() {
		return _encoding;
	}

	public int getFixedValueWidth() {
		return _fixedValueWidth;
	}

	public int[] getValueWidths() {
		return _valueWidths;
	}

	@Override
	public String getFilename() {
		return _filename;
	}

	public boolean isFailOnInconsistencies() {
		return _failOnInconsistencies;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_filename);
		identifiers.add(_encoding);
		identifiers.add(_fixedValueWidth);
		identifiers.add(_valueWidths);
		identifiers.add(_failOnInconsistencies);
	}

	@Override
	public String toString() {
		return "FixedWidthDatastore[name=" + getName() + ", filename=" + _filename + ", encoding=" + _encoding
				+ ", valueWidths=" + Arrays.toString(_valueWidths) + ", fixedValueWidth=" + _fixedValueWidth + "]";
	}
}
