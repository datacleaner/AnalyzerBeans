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
package org.eobjects.analyzer.descriptors;

import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.filter.NullCheckFilter;

public class AnnotationBasedFilterDescriptorTest extends TestCase {

	private FilterBeanDescriptor<NullCheckFilter, NullCheckFilter.NullCheckCategory> desc = Descriptors.ofFilter(NullCheckFilter.class);

	public void testGetCategoryEnum() throws Exception {
		Class<NullCheckFilter.NullCheckCategory> categoryEnum = desc.getOutcomeCategoryEnum();

		assertEquals(NullCheckFilter.NullCheckCategory.class, categoryEnum);
	}

	public void testGetCategoryNames() throws Exception {
		Set<String> categoryNames = desc.getOutcomeCategoryNames();
		categoryNames = new TreeSet<String>(categoryNames);
		assertEquals("[NOT_NULL, NULL]", categoryNames.toString());
	}
}
