package org.eobjects.analyzer.reference;

public interface ReferenceDataCatalog {

	public String[] getDictionaryNames();
	
	public Dictionary getDictionary(String name);

	public String[] getSynonymCatalogNames();
	
	public SynonymCatalog getSynonymCatalog(String name);
}
