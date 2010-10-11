package org.eobjects.analyzer.reference;

import java.io.Serializable;

final class TextBasedSynonym implements Synonym, Serializable {

	private static final long serialVersionUID = 1L;

	private final String[] _synonyms;
	private final boolean _caseSensitive;

	public TextBasedSynonym(String line, boolean caseSensitive) {
		String[] split = line.split("\\,");
		_synonyms = split;
		_caseSensitive = caseSensitive;
	}

	@Override
	public String getMasterTerm() {
		return _synonyms[0];
	}

	@Override
	public ReferenceValues<String> getSynonyms() {
		return new SimpleStringReferenceValues(_synonyms, _caseSensitive);
	}

}
