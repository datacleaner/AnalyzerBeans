package org.eobjects.analyzer.reference;

final class TextBasedSynonym implements Synonym {

	private final String _masterTerm;
	private final String[] _synonyms;

	public TextBasedSynonym(String line) {
		String[] split = line.split("\\,");
		_masterTerm = split[0];
		_synonyms = split;
	}

	@Override
	public String getMasterTerm() {
		return _masterTerm;
	}

	@Override
	public ReferenceValues<String> getSynonyms() {
		return new SimpleReferenceValues<String>(_synonyms);
	}

}
