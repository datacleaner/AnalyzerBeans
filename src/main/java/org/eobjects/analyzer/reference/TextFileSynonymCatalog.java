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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.analyzer.util.filemonitor.FileMonitor;
import org.eobjects.analyzer.util.filemonitor.FileMonitorFactory;
import org.eobjects.metamodel.util.FileHelper;

public final class TextFileSynonymCatalog implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	private transient volatile Map<String, String> _masterTermCache;
	private transient File _file;
	private transient FileMonitor _fileMonitor;

	private final String _filename;
	private final String _name;
	private final boolean _caseSensitive;
	private final String _encoding;

	public TextFileSynonymCatalog(String name, String filename, boolean caseSensitive, String encoding) {
		_name = name;
		_filename = filename;
		_caseSensitive = caseSensitive;
		_encoding = encoding;
	}

	public TextFileSynonymCatalog(String name, File file, boolean caseSensitive, String encoding) {
		_name = name;
		_filename = file.getPath();
		_caseSensitive = caseSensitive;
		_encoding = encoding;
	}

	@Override
	public String getName() {
		return _name;
	}

	public String getEncoding() {
		return _encoding;
	}

	public String getFilename() {
		return _filename;
	}

	public boolean isCaseSensitive() {
		return _caseSensitive;
	}

	@Override
	public Collection<Synonym> getSynonyms() {
		BufferedReader reader = FileHelper.getBufferedReader(_file, _encoding);
		try {
			List<Synonym> synonyms = new ArrayList<Synonym>();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				synonyms.add(new TextFileSynonym(line, _caseSensitive));
			}
			return synonyms;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			FileHelper.safeClose(_file);
		}
	}
	
	private Map<String, String> getMasterTermCache() {
		if (_masterTermCache == null) {
			synchronized (this) {
				if (_masterTermCache == null) {
					_masterTermCache = CollectionUtils.createCacheMap();
					_file = new File(_filename);
					_fileMonitor = FileMonitorFactory.getFileMonitor(_file);
				}
			}
		} else {
			if (_fileMonitor.hasChanged()) {
				// reset the cache
				_masterTermCache = CollectionUtils.createCacheMap();
			}
		}
		return _masterTermCache;
	}

	@Override
	public String getMasterTerm(String term) {
		if (StringUtils.isNullOrEmpty(term)) {
			return null;
		}
		String masterTerm = getMasterTermCache().get(term);
		if (masterTerm != null) {
			return masterTerm;
		}

		BufferedReader reader = FileHelper.getBufferedReader(_file, _encoding);
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				TextFileSynonym synonym = new TextFileSynonym(line, _caseSensitive);
				masterTerm = synonym.getMasterTerm();
				if (term.equals(masterTerm) || synonym.getSynonyms().containsValue(term)) {
					getMasterTermCache().put(term, masterTerm);
					return masterTerm;
				}
			}
			return null;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			FileHelper.safeClose(reader);
		}
	}
}
