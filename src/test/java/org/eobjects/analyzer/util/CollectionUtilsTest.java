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
package org.eobjects.analyzer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eobjects.metamodel.util.ToStringComparator;

import junit.framework.TestCase;

public class CollectionUtilsTest extends TestCase {

	public void testArray1() throws Exception {
		String[] result = CollectionUtils.array(new String[] { "foo", "bar" }, "hello", "world");
		assertEquals("[foo, bar, hello, world]", Arrays.toString(result));
	}

	public void testArray2() throws Exception {
		Object existingArray = new Object[] { 'c' };
		Object[] result = CollectionUtils.array(Object.class, existingArray, "foo", 1, "bar");

		assertEquals("[c, foo, 1, bar]", Arrays.toString(result));
	}

	public void testSorted() throws Exception {
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add("4");
		list1.add("1");
		list1.add("3");
		list1.add("2");

		List<String> list2 = CollectionUtils.sorted(list1, ToStringComparator.getComparator());
		assertEquals("[4, 1, 3, 2]", list1.toString());
		assertEquals("[1, 2, 3, 4]", list2.toString());
	}
}
