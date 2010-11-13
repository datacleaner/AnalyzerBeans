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

import junit.framework.TestCase;

public class CompareUtilsTest extends TestCase {

	public void testEquals() throws Exception {
		assertTrue(CompareUtils.equals(null, null));
		assertTrue(CompareUtils.equals("hello", "hello"));
		assertFalse(CompareUtils.equals("hello", null));
		assertFalse(CompareUtils.equals(null, "hello"));
		assertFalse(CompareUtils.equals("world", "hello"));

		MyCloneable o1 = new MyCloneable();
		assertTrue(CompareUtils.equals(o1, o1));
		MyCloneable o2 = o1.clone();
		assertFalse(CompareUtils.equals(o1, o2));
	}

	public void testCompare() throws Exception {
		assertEquals(0, CompareUtils.compare(null, null));
		assertEquals(-1, CompareUtils.compare(null, "hello"));
		assertEquals(1, CompareUtils.compare("hello", null));
		assertEquals(0, CompareUtils.compare("hello", "hello"));
		assertEquals("hello".compareTo("world"),
				CompareUtils.compare("hello", "world"));
	}

	static final class MyCloneable implements Cloneable {
		@Override
		public boolean equals(Object obj) {
			return false;
		}

		@Override
		public MyCloneable clone() {
			try {
				return (MyCloneable) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new UnsupportedOperationException();
			}
		}
	};
}
