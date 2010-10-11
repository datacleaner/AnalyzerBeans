package org.eobjects.analyzer.reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import dk.eobjects.metamodel.util.FileHelper;

public final class TextBasedSynonymCatalog implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	private final String _filename;
	private final String _name;
	private final boolean _caseSensitive;
	private final WeakHashMap<String, String> _masterTermCache = new WeakHashMap<String, String>();
	private File _file;

	public TextBasedSynonymCatalog(String name, String filename, boolean caseSensitive) {
		_name = name;
		_filename = filename;
		_caseSensitive = caseSensitive;
	}

	public TextBasedSynonymCatalog(String name, File file, boolean caseSensitive) {
		_name = name;
		_filename = file.getPath();
		_file = file;
		_caseSensitive = caseSensitive;
	}

	private File getFile() {
		if (_file == null) {
			_file = new File(_filename);
		}
		return _file;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Collection<Synonym> getSynonyms() {
		BufferedReader reader = FileHelper.getBufferedReader(getFile());
		try {
			List<Synonym> synonyms = new ArrayList<Synonym>();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				synonyms.add(new TextBasedSynonym(line, _caseSensitive));
			}
			return synonyms;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getMasterTerm(String term) {
		String masterTerm = _masterTermCache.get(term);
		if (masterTerm != null) {
			return masterTerm;
		}

		BufferedReader reader = FileHelper.getBufferedReader(getFile());
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				TextBasedSynonym synonym = new TextBasedSynonym(line, _caseSensitive);
				masterTerm = synonym.getMasterTerm();
				if (term.equals(masterTerm) || synonym.getSynonyms().containsValue(term)) {
					_masterTermCache.put(term, masterTerm);
					return masterTerm;
				}
			}
			return null;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
