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
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;

/**
 * Datastore based on fixed width files
 * 
 * @author Kasper Sørensen
 * 
 */
public class FixedWidthDatastore extends UsageAwareDatastore implements FileDatastore {

	private static final long serialVersionUID = 1L;

	private final String _filename;
	private final String _encoding;
	private final int _fixedValueWidth;

	public FixedWidthDatastore(String name, String filename, String encoding, int fixedValueWidth) {
		super(name);
		_filename = filename;
		_encoding = encoding;
		_fixedValueWidth = fixedValueWidth;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, FixedWidthDatastore.class).readObject(stream);
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(false);
	}

	@Override
	protected UsageAwareDataContextProvider createDataContextProvider() {
		File file = new File(_filename);
		assert file.exists();

		DataContext dataContext = DataContextFactory.createFixedWidthDataContext(file, _encoding, _fixedValueWidth);
		return new SingleDataContextProvider(dataContext, this);
	}

	public String getEncoding() {
		return _encoding;
	}

	public int getFixedValueWidth() {
		return _fixedValueWidth;
	}

	@Override
	public String getFilename() {
		return _filename;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_filename);
		identifiers.add(_encoding);
		identifiers.add(_fixedValueWidth);
	}

	@Override
	public String toString() {
		return "FixedWidthDatastore[name=" + getName() + ", filename=" + _filename + ", encoding=" + _encoding
				+ ", fixedValueWidth=" + _fixedValueWidth + "]";
	}
}
