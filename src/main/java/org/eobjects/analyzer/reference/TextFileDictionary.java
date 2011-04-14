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
package org.eobjects.analyzer.reference;

import java.io.BufferedReader;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.metamodel.util.FileHelper;

public class TextFileDictionary implements Dictionary {

	private static final long serialVersionUID = 1L;

	private transient File _file;
	private final String _name;
	private final String _filename;
	private final String _encoding;

	private Set<String> _entries = null;

	public TextFileDictionary(String name, String filename, String encoding) {
		_name = name;
		_filename = filename;
		_encoding = encoding;
	}

	private File getFile() {
		if (_file == null) {
			synchronized (this) {
				if (_file == null) {
					_file = new File(_filename);
				}
			}
		}
		return _file;
	}

	@Override
	public String getName() {
		return _name;
	}

	public String getFilename() {
		return _filename;
	}

	public String getEncoding() {
		return _encoding;
	}

	@Initialize
	public void init() {
		Set<String> entries = new HashSet<String>();
		BufferedReader reader = null;
		try {
			reader = FileHelper.getBufferedReader(getFile(), _encoding);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				entries.add(line);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			FileHelper.safeClose(reader);
		}
		_entries = Collections.unmodifiableSet(entries);
	}

	@Close
	public void close() {
		_entries = null;
	}

	@Override
	public boolean containsValue(String value) {
		if (_entries == null) {
			init();
		}
		if (value == null) {
			return false;
		}
		return _entries.contains(value);
	}

	@Override
	public ReferenceValues<String> getValues() {
		if (_entries == null) {
			init();
		}
		return new SimpleStringReferenceValues(_entries, true);
	}
}
