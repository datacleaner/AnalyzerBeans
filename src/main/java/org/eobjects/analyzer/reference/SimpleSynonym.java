package org.eobjects.analyzer.reference;

import java.io.Serializable;

public class SimpleSynonym implements Synonym, Serializable {
	
	private static final long serialVersionUID = 1L;

	private String _masterTerm;
	private ReferenceValues<String> _synonyms;

	public SimpleSynonym(String masterTerm, String... synonyms) {
		_masterTerm = masterTerm;
		_synonyms = new SimpleReferenceValues<String>(synonyms);
	}

	@Override
	public String getMasterTerm() {
		return _masterTerm;
	}

	@Override
	public ReferenceValues<String> getSynonyms() {
		return _synonyms;
	}

}
