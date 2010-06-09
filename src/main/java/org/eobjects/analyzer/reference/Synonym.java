package org.eobjects.analyzer.reference;

public interface Synonym {

	public String getMasterTerm();

	public ReferenceValues<String> getSynonyms();
}
