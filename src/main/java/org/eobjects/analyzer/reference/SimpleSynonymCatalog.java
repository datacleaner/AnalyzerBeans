package org.eobjects.analyzer.reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SimpleSynonymCatalog implements SynonymCatalog, Serializable {

	private static final long serialVersionUID = 1L;

	private String _name;
	private List<Synonym> _synonyms;

	public SimpleSynonymCatalog(String name) {
		_name = name;
		_synonyms = new ArrayList<Synonym>();
	}

	public SimpleSynonymCatalog(String name, Synonym... synonyms) {
		_name = name;
		_synonyms = new ArrayList<Synonym>(Arrays.asList(synonyms));
	}

	public SimpleSynonymCatalog(String name, List<Synonym> synonyms) {
		_name = name;
		_synonyms = synonyms;
	}

	@Override
	public String getMasterTerm(String term) {
		for (Synonym synonym : _synonyms) {
			if (synonym.getSynonyms().containsValue(term)) {
				return synonym.getMasterTerm();
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Collection<Synonym> getSynonyms() {
		return _synonyms;
	}

}
