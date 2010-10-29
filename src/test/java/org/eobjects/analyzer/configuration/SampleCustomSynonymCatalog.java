package org.eobjects.analyzer.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.reference.SimpleSynonym;
import org.eobjects.analyzer.reference.Synonym;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.junit.Ignore;

@Ignore
public class SampleCustomSynonymCatalog implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	@Configured
	String name;

	@Configured
	String[][] values;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<Synonym> getSynonyms() {
		List<Synonym> result = new ArrayList<Synonym>();
		for (String[] strings : values) {
			result.add(new SimpleSynonym(strings[0], strings));
		}
		return result;
	}

	@Override
	public String getMasterTerm(String term) {
		for (Synonym synonym : getSynonyms()) {
			if (synonym.getSynonyms().containsValue(term)) {
				return synonym.getMasterTerm();
			}
		}
		return null;
	}

}
