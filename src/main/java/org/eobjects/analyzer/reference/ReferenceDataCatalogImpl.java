package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReferenceDataCatalogImpl implements ReferenceDataCatalog {

	private static final long serialVersionUID = 1L;
	private Collection<Dictionary> _dictionaries;
	private Collection<SynonymCatalog> _synonymCatalogs;

	public ReferenceDataCatalogImpl() {
		this(new ArrayList<Dictionary>(), new ArrayList<SynonymCatalog>());
	}

	public ReferenceDataCatalogImpl(Collection<Dictionary> dictionaries, Collection<SynonymCatalog> synonymCatalogs) {
		if (dictionaries == null) {
			throw new IllegalArgumentException("dictionaries cannot be null");
		}
		Set<String> uniqueNames = new HashSet<String>();
		for (Dictionary dictionary : dictionaries) {
			String name = dictionary.getName();
			if (uniqueNames.contains(name)) {
				throw new IllegalStateException("Duplicate dictionary names: " + name);
			} else {
				uniqueNames.add(name);
			}
		}
		if (synonymCatalogs == null) {
			throw new IllegalArgumentException("synonymCatalogs cannot be null");
		}
		uniqueNames.clear();
		for (SynonymCatalog synonymCatalog : synonymCatalogs) {
			String name = synonymCatalog.getName();
			if (uniqueNames.contains(name)) {
				throw new IllegalStateException("Duplicate synonym catalog names: " + name);
			} else {
				uniqueNames.add(name);
			}
		}
		_dictionaries = dictionaries;
		_synonymCatalogs = synonymCatalogs;
	}

	@Override
	public String[] getDictionaryNames() {
		String[] names = new String[_dictionaries.size()];
		int i = 0;
		for (Dictionary d : _dictionaries) {
			names[i] = d.getName();
			i++;
		}
		return names;
	}

	@Override
	public Dictionary getDictionary(String name) {
		if (name != null) {
			for (Dictionary d : _dictionaries) {
				if (name.equals(d.getName())) {
					return d;
				}
			}
		}
		return null;
	}

	@Override
	public String[] getSynonymCatalogNames() {
		String[] names = new String[_synonymCatalogs.size()];
		int i = 0;
		for (SynonymCatalog sc : _synonymCatalogs) {
			names[i] = sc.getName();
			i++;
		}
		return names;
	}

	@Override
	public SynonymCatalog getSynonymCatalog(String name) {
		if (name != null) {
			for (SynonymCatalog sc : _synonymCatalogs) {
				if (name.equals(sc.getName())) {
					return sc;
				}
			}
		}
		return null;
	}

}
