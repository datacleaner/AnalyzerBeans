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
