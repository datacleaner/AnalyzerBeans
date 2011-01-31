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

import java.util.Arrays;

import junit.framework.TestCase;

public class SimpleSynonymCatalogTest extends TestCase {

	public void testGetMasterTerm() throws Exception {
		SimpleSynonymCatalog sc = new SimpleSynonymCatalog("countries", Arrays.asList(new Synonym[] {
				new SimpleSynonym("DNK", "Denmark"), new SimpleSynonym("NLD", "The netherlands") }));

		assertEquals("DNK", sc.getMasterTerm("DNK"));
		assertEquals("NLD", sc.getMasterTerm("NLD"));
		assertEquals("DNK", sc.getMasterTerm("Denmark"));
		assertEquals("NLD", sc.getMasterTerm("The netherlands"));
		assertNull(sc.getMasterTerm("Danemark"));
	}
}
