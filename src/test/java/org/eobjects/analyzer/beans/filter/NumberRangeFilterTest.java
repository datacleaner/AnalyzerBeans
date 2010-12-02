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

public class NumberRangeFilterTest extends TestCase {

	public void testFilter() throws Exception {
		NumberRangeFilter filter = new NumberRangeFilter(5d, 10d);
		assertEquals(RangeFilterCategory.LOWER, filter.categorize((Number) null));
		assertEquals(RangeFilterCategory.LOWER, filter.categorize(0));
		assertEquals(RangeFilterCategory.LOWER, filter.categorize(-200));
		assertEquals(RangeFilterCategory.VALID, filter.categorize(5));
		assertEquals(RangeFilterCategory.VALID, filter.categorize(5.0));
		assertEquals(RangeFilterCategory.VALID, filter.categorize(5.0f));
		assertEquals(RangeFilterCategory.VALID, filter.categorize(10));
		assertEquals(RangeFilterCategory.VALID, filter.categorize(10.0));
		assertEquals(RangeFilterCategory.VALID, filter.categorize(10.0f));
		assertEquals(RangeFilterCategory.HIGHER, filter.categorize(11));
	}
}
