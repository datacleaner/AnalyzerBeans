package org.eobjects.analyzer.reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dk.eobjects.metamodel.util.FileHelper;

public final class TextBasedSynonymCatalog implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	private final String _filename;
	private final String _name;
	private File _file;

	public TextBasedSynonymCatalog(String name, String filename) {
		_name = name;
		_filename = filename;
	}

	public TextBasedSynonymCatalog(String name, File file) {
		_name = name;
		_filename = file.getPath();
		_file = file;
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
				synonyms.add(new TextBasedSynonym(line));
			}
			return synonyms;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getMasterTerm(String term) {
		BufferedReader reader = FileHelper.getBufferedReader(getFile());
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				TextBasedSynonym synonym = new TextBasedSynonym(line);
				if (term.equals(synonym.getMasterTerm()) || synonym.getSynonyms().containsValue(term)) {
					return synonym.getMasterTerm();
				}
			}
			return null;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
