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

import java.util.Date;

import org.eobjects.metamodel.util.DateUtils;
import org.eobjects.metamodel.util.Month;

import junit.framework.TestCase;

public class DateRangeFilterTest extends TestCase {

    public void testFilter() throws Exception {
        DateRangeFilter filter = new DateRangeFilter(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2011, Month.JANUARY, 1));
        filter.validate();
        assertEquals(RangeFilterCategory.LOWER, filter.categorize((Date) null));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(DateUtils.get(2009, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(DateUtils.get(0, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2010, Month.FEBRUARY, 1)));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2010, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2011, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.HIGHER, filter.categorize(DateUtils.get(2012, Month.JANUARY, 1)));
    }

    public void testSameMaxAndMin() throws Exception {
        DateRangeFilter filter = new DateRangeFilter(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2010, Month.JANUARY, 1));
        filter.validate();
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2010, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(DateUtils.get(2009, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.HIGHER, filter.categorize(DateUtils.get(2012, Month.JANUARY, 1)));
    }
}
