package org.eobjects.analyzer.reference;

import java.io.Serializable;
import java.util.Collection;

public interface SynonymCatalog extends Serializable {

	/**
	 * @return The name of this synonym catalog
	 */
	public String getName();

	/**
	 * @return all synonyms contained within this catalog
	 */
	public Collection<Synonym> getSynonyms();

	/**
	 * Searches the catalog for a replacement (master) term for a given term
	 * 
	 * @param term
	 *            the term which is suspected to be a synonym of a master term
	 * @return the master term found, or null if none is found
	 */
	public String getMasterTerm(String term);
}
