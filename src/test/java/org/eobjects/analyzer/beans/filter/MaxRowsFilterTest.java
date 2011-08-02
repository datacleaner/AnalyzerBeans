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

import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

public class MaxRowsFilterTest extends TestCase {

	public void testDescriptor() throws Exception {
		FilterBeanDescriptor<MaxRowsFilter, ValidationCategory> desc = Descriptors.ofFilter(MaxRowsFilter.class);

		assertEquals("Max rows", desc.getDisplayName());
	}

	public void testCounter() throws Exception {
		MaxRowsFilter f = new MaxRowsFilter(3);
		assertEquals(ValidationCategory.VALID, f.categorize(new MockInputRow()));
		assertEquals(ValidationCategory.VALID, f.categorize(new MockInputRow()));
		assertEquals(ValidationCategory.VALID, f.categorize(new MockInputRow()));
		assertEquals(ValidationCategory.INVALID, f.categorize(new MockInputRow()));
	}
}
