package org.eobjects.analyzer.lifecycle;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class HsqldbCollectionProviderTest extends TestCase {

	private HsqldbCollectionProvider cp = new HsqldbCollectionProvider();

	public void testCreateList() throws Exception {
		List<String> list = cp.createList(String.class);
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());

		list.add("hello");
		list.add("world");
		assertEquals(2, list.size());

		assertEquals("world", list.get(1));
		
		assertEquals("[hello, world]", Arrays.toString(list.toArray()));
		
		list.remove(1);
		
		assertEquals("[hello]", Arrays.toString(list.toArray()));
		
		list.remove("foobar");
		list.remove("hello");
		
		assertEquals("[]", Arrays.toString(list.toArray()));

		cp.cleanUp(list);
	}

	public void testCreateMap() throws Exception {
		Map<Integer, String> map = cp.createMap(Integer.class, String.class);

		map.put(1, "hello");
		map.put(2, "world");
		map.put(5, "foo");

		assertEquals("world", map.get(2));
		assertNull(map.get(3));

		assertEquals(3, map.size());

		// override 5
		map.put(5, "bar");

		assertEquals(3, map.size());
		
		cp.cleanUp(map);
	}

	public void testCreateSet() throws Exception {
		Set<Long> set = cp.createSet(Long.class);

		assertTrue(set.isEmpty());
		assertEquals(0, set.size());

		set.add(1l);

		assertEquals(1, set.size());

		set.add(2l);
		set.add(3l);

		assertEquals(3, set.size());

		set.add(3l);

		assertEquals(3, set.size());

		Iterator<Long> it = set.iterator();
		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(1l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(2l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(3l), it.next());

		assertFalse(it.hasNext());

		assertFalse(it.hasNext());

		it = set.iterator();

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(1l), it.next());

		// remove 1
		it.remove();

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(2l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(3l), it.next());

		assertFalse(it.hasNext());

		assertEquals("[2, 3]", Arrays.toString(set.toArray()));
		
		cp.cleanUp(set);
	}
}
