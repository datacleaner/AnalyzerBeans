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
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.DateGapAnalyzer;
import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

public class ConfiguredPropertyDescriptorImplTest extends TestCase {

	private FilterBeanDescriptor<MockFilter, ValidationCategory> _descriptor;

	@Configured
	String str1;

	@Configured
	String str2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_descriptor = Descriptors.ofFilter(MockFilter.class);
	}

	public void testCompareTo() throws Exception {
		Set<ConfiguredPropertyDescriptor> properties = Descriptors.ofAnalyzer(DateGapAnalyzer.class)
				.getConfiguredProperties();
		assertEquals(5, properties.size());
		Iterator<ConfiguredPropertyDescriptor> it = properties.iterator();
		assertTrue(it.hasNext());
		assertEquals("From column", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("To column", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Group column", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Count intersecting from and to dates as overlaps", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Fault tolerant switch from/to dates", it.next().getName());
		assertFalse(it.hasNext());

		Field f1 = getClass().getDeclaredField("str1");
		Field f2 = getClass().getDeclaredField("str2");

		ConfiguredPropertyDescriptorImpl d1 = new ConfiguredPropertyDescriptorImpl(f1, null);
		ConfiguredPropertyDescriptorImpl d2 = new ConfiguredPropertyDescriptorImpl(f2, null);
		assertTrue(d1.compareTo(d2) < 0);
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

	public void testGetConfiguredPropertyByAlias() throws Exception {
		ConfiguredPropertyDescriptor cp1 = _descriptor.getConfiguredProperty("Some file");
		ConfiguredPropertyDescriptor cp2 = _descriptor.getConfiguredProperty("a file");
		assertSame(cp1, cp2);
	}

	@FilterBean("Mock filter")
	private class MockFilter implements Filter<ValidationCategory> {

		@Configured
		@Alias("a file")
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
