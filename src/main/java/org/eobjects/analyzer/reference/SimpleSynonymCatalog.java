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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.util.StringUtils;

public final class SimpleSynonymCatalog implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final List<Synonym> _synonyms;

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
		if (StringUtils.isNullOrEmpty(term)) {
			return null;
		}
		for (Synonym synonym : _synonyms) {
			if (synonym.getMasterTerm().equals(term)) {
				return synonym.getMasterTerm();
			}
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
