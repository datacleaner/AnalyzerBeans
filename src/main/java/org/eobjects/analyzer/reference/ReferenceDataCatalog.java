package org.eobjects.analyzer.reference;

import java.io.Serializable;

public interface ReferenceDataCatalog extends Serializable {

	public String[] getDictionaryNames();

	public Dictionary getDictionary(String name);

	public String[] getSynonymCatalogNames();

	public SynonymCatalog getSynonymCatalog(String name);
}
