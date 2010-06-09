package org.eobjects.analyzer.lifecycle;

import java.util.HashMap;

import junit.framework.TestCase;

public class ProvidedListTest extends TestCase {

	public void testAdd() throws Exception {
		ProvidedList<String> list = new ProvidedList<String>(
				new HashMap<Integer, String>());

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
		ProvidedList<String> list = new ProvidedList<String>(
				new HashMap<Integer, String>());

		list.add("foo1");
		list.add("foo2");
		list.add("foo3");
		list.add("foo4");

		list.set(1, "foobar");

		assertEquals("[foo1, foobar, foo3, foo4]", list.toString());
	}

	public void testRemove() throws Exception {
		ProvidedList<String> list = new ProvidedList<String>(
				new HashMap<Integer, String>());

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
