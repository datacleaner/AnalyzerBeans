package org.eobjects.analyzer.lifecycle;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.storage.BerkeleyDbStorageProvider;

import junit.framework.TestCase;

public class BerkeleyDbCollectionProviderTest extends TestCase {

	BerkeleyDbStorageProvider handler = new BerkeleyDbStorageProvider();

	public void testCreateMap() throws Exception {
		Map<String, Long> map = handler.createMap(String.class, Long.class);
		assertNotNull(map);
		
		handler.cleanUp(map);
	}

	public void testCreateList() throws Exception {
		List<String> list = handler.createList(String.class);
		assertNotNull(list);
		list.add("hello");
		list.add("hello");
		assertEquals(2, list.size());
		list.add("hi");
		assertEquals(3, list.size());
		
		handler.cleanUp(list);
	}
	
	public void testCreateSet() throws Exception {
		Set<String> set = handler.createSet(String.class);
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
		
		handler.cleanUp(set);
	}
}
