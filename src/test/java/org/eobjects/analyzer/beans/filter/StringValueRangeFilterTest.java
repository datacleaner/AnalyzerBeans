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
package org.eobjects.analyzer.beans.filter;

import junit.framework.TestCase;

public class StringValueRangeFilterTest extends TestCase {

	public void testCategorize() throws Exception {
		StringValueRangeFilter f = new StringValueRangeFilter("AAA", "ccc");
		f.init();

		assertEquals(RangeFilterCategory.LOWER, f.categorize((String) null));
		assertEquals(RangeFilterCategory.LOWER, f.categorize(""));
		assertEquals(RangeFilterCategory.VALID, f.categorize("XXX"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("BBB"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("aaa"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("bbb"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("ccc"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("AAA"));
		assertEquals(RangeFilterCategory.VALID, f.categorize("CCC"));
		assertEquals(RangeFilterCategory.HIGHER, f.categorize("ccd"));
		assertEquals(RangeFilterCategory.HIGHER, f.categorize("xxx"));
	}
}
