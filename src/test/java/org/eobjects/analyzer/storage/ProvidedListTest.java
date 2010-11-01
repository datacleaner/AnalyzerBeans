package org.eobjects.analyzer.storage;

import java.util.HashMap;

import org.eobjects.analyzer.storage.BerkeleyDbList;

import junit.framework.TestCase;

public class ProvidedListTest extends TestCase {

	private BerkeleyDbStorageProvider sp = new BerkeleyDbStorageProvider();

	public void testAdd() throws Exception {
		BerkeleyDbList<String> list = new BerkeleyDbList<String>(sp, new HashMap<Integer, String>());

		list.add("foo1");
		list.add("foo2");
		list.add("foo3");
		list.add("foo4");

		assertEquals("[foo1, foo2, foo3, foo4]", list.toString());

		list.add(2, "foo5");

		assertEquals("[foo1, foo2, foo5, foo3, foo4]", list.toString());

		list.add(null);

		assertEquals("[foo1, foo2, foo5, foo3, foo4, null]", list.toString());
	}

	public void testSet() throws Exception {
		BerkeleyDbList<String> list = new BerkeleyDbList<String>(sp, new HashMap<Integer, String>());

		list.add("foo1");
		list.add("foo2");
		list.add("foo3");
		list.add("foo4");

		list.set(1, "foobar");

		assertEquals("[foo1, foobar, foo3, foo4]", list.toString());
	}

	public void testRemove() throws Exception {
		BerkeleyDbList<String> list = new BerkeleyDbList<String>(sp, new HashMap<Integer, String>());

		list.add("foo1");
		list.add("foo2");
		list.add("foo3");
		list.add("foo4");

		list.remove("foo2");

		assertEquals("[foo1, foo3, foo4]", list.toString());

		list.remove(0);

		assertEquals("[foo3, foo4]", list.toString());

		list.remove(1);

		assertEquals("[foo3]", list.toString());
	}
}
