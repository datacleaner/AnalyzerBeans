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

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.AnnotationBasedFilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

import junit.framework.TestCase;

public class NotNullFilterTest extends TestCase {

	public void testCategorize() throws Exception {
		InputColumn<Integer> col1 = new MockInputColumn<Integer>("col1", Integer.class);
		InputColumn<Boolean> col2 = new MockInputColumn<Boolean>("col2", Boolean.class);
		InputColumn<String> col3 = new MockInputColumn<String>("col3", String.class);
		InputColumn<?>[] columns = new InputColumn[] { col1, col2, col3 };

		NotNullFilter filter = new NotNullFilter(columns, true);
		assertEquals(ValidationCategory.VALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "foo")));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, null).put(col3, "foo")));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "")));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, null)));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, null).put(col2, null).put(col3, null)));
	}
	
	public void testDescriptor() throws Exception {
		FilterBeanDescriptor<NotNullFilter, ValidationCategory> desc = AnnotationBasedFilterBeanDescriptor.create(NotNullFilter.class);
		Class<ValidationCategory> categoryEnum = desc.getCategoryEnum();
		assertEquals(ValidationCategory.class, categoryEnum);
	}
}
