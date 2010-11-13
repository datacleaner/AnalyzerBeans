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

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

public class ConfiguredPropertyDescriptorTest extends TestCase {

	private FilterBeanDescriptor<MockFilter, ValidationCategory> _descriptor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_descriptor = AnnotationBasedFilterBeanDescriptor.create(MockFilter.class);
	}

	public void testEnum() throws Exception {
		Set<ConfiguredPropertyDescriptor> properties = _descriptor.getConfiguredProperties();
		assertEquals(3, properties.size());

		ConfiguredPropertyDescriptor cp = _descriptor.getConfiguredProperty("Some enum");
		assertFalse(cp.isArray());
		assertTrue(cp.getType().isEnum());

		MockFilter filter = new MockFilter();
		assertNull(filter.getSomeEnum());
		cp.setValue(filter, ValidationCategory.VALID);
		assertEquals(ValidationCategory.VALID, filter.getSomeEnum());
	}

	public void testFile() throws Exception {
		ConfiguredPropertyDescriptor cp = _descriptor.getConfiguredProperty("Some file");
		assertFalse(cp.isArray());
		assertTrue(cp.getType() == File.class);
		
		MockFilter filter = new MockFilter();
		assertNull(filter.getSomeFile());
		cp.setValue(filter, new File("."));
		assertEquals(new File("."), filter.getSomeFile());
	}

	@FilterBean("Mock filter")
	private class MockFilter implements Filter<ValidationCategory> {

		@Configured
		File someFile;

		@Configured
		ValidationCategory someEnum;

		@SuppressWarnings("unused")
		@Configured
		InputColumn<?> input;

		@Override
		public ValidationCategory categorize(InputRow inputRow) {
			return someEnum;
		}

		public ValidationCategory getSomeEnum() {
			return someEnum;
		}

		public File getSomeFile() {
			return someFile;
		}
	}
}
