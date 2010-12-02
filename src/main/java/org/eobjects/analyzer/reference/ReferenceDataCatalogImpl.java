/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReferenceDataCatalogImpl implements ReferenceDataCatalog {

	private static final long serialVersionUID = 1L;
	private final Collection<Dictionary> _dictionaries;
	private final Collection<SynonymCatalog> _synonymCatalogs;
	private final Collection<StringPattern> _stringPatterns;

	public ReferenceDataCatalogImpl() {
		this(new ArrayList<Dictionary>(), new ArrayList<SynonymCatalog>(), new ArrayList<StringPattern>());
	}

	public ReferenceDataCatalogImpl(Collection<Dictionary> dictionaries, Collection<SynonymCatalog> synonymCatalogs,
			Collection<StringPattern> stringPatterns) {
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

		if (stringPatterns == null) {
			throw new IllegalArgumentException("stringPatterns cannot be null");
		}
		uniqueNames.clear();
		for (StringPattern stringPattern : stringPatterns) {
			String name = stringPattern.getName();
			if (uniqueNames.contains(name)) {
				throw new IllegalStateException("Duplicate string pattern names: " + name);
			} else {
				uniqueNames.add(name);
			}
		}
		_dictionaries = dictionaries;
		_synonymCatalogs = synonymCatalogs;
		_stringPatterns = stringPatterns;
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

	@Override
	public StringPattern getStringPattern(String name) {
		if (name != null) {
			for (StringPattern sp : _stringPatterns) {
				if (name.equals(sp.getName())) {
					return sp;
				}
			}
		}
		return null;
	}

	@Override
	public String[] getStringPatternNames() {
		String[] names = new String[_stringPatterns.size()];
		int i = 0;
		for (StringPattern sp : _stringPatterns) {
			names[i] = sp.getName();
			i++;
		}
		return names;
	}
}
