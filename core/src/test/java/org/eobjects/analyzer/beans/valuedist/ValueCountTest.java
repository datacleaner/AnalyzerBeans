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
package org.eobjects.analyzer.beans.valuedist;

import junit.framework.TestCase;

public class ValueCountTest extends TestCase {

	public void testEqualsAndHashCode() throws Exception {
		ValueCount vc1 = new ValueCount("foo", 1337);
		ValueCount vc2 = new ValueCount("foo", 1337);
		ValueCount vc3 = new ValueCount(null, 1337);
		ValueCount vc4 = new ValueCount("foo", 1338);
		
		assertEquals(vc1, vc2);
		assertEquals(vc1.hashCode(), vc2.hashCode());
		assertTrue(!vc1.equals(vc3));
		assertTrue(!vc1.equals(vc4));
	}
}
