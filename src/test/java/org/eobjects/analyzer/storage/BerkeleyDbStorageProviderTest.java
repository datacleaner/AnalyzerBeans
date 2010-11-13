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
package org.eobjects.analyzer.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.storage.BerkeleyDbStorageProvider;

import junit.framework.TestCase;

public class BerkeleyDbStorageProviderTest extends TestCase {

	private BerkeleyDbStorageProvider sp = new BerkeleyDbStorageProvider();

	public void testCreateMap() throws Exception {
		Map<String, Long> map = sp.createMap(String.class, Long.class);
		assertNotNull(map);
		
		sp.cleanUp(map);
	}

	public void testCreateList() throws Exception {
		List<String> list = sp.createList(String.class);
		assertNotNull(list);
		list.add("hello");
		list.add("hello");
		assertEquals(2, list.size());
		list.add("hi");
		assertEquals(3, list.size());
		
		sp.cleanUp(list);
	}
	
	public void testCreateSet() throws Exception {
		Set<String> set = sp.createSet(String.class);
		set.add("hello");
		set.add("hello");
		assertEquals(1, set.size());
		
		set.add("world");
		set.add("world");
		set.add("world");
		set.add("world");
		assertEquals(2, set.size());
		
		set.remove("world");
		assertEquals(1, set.size());
		
		sp.cleanUp(set);
	}
}
