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

import junit.framework.TestCase;

public class TextBasedSynonymCatalogTest extends TestCase {

	public void testCountrySynonyms() throws Exception {
		SynonymCatalog cat = new TextBasedSynonymCatalog("foobar", "src/test/resources/synonym-countries.txt", true, "UTF-8");
		assertNull(cat.getMasterTerm("foobar"));
		assertEquals("DNK", cat.getMasterTerm("Denmark"));
		assertEquals("GBR", cat.getMasterTerm("England"));

		assertEquals("GBR", cat.getMasterTerm("GBR"));
		assertEquals("DNK", cat.getMasterTerm("DNK"));
	}
}
