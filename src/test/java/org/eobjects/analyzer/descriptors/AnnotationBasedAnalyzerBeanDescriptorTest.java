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

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.MatchingAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock;
import org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceData;
import org.eobjects.analyzer.result.AnalyzerResult;

public class AnnotationBasedAnalyzerBeanDescriptorTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ExploringAnalyzerMock.clearInstances();
		RowProcessingAnalyzerMock.clearInstances();
	}

	public void testExploringType() throws Exception {
		AnalyzerBeanDescriptor<?> descriptor = AnnotationBasedAnalyzerBeanDescriptor.create(ExploringAnalyzerMock.class);
		assertEquals(true, descriptor.isExploringAnalyzer());
		assertEquals(false, descriptor.isRowProcessingAnalyzer());

		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
		Iterator<ConfiguredPropertyDescriptor> it = configuredProperties.iterator();
		assertTrue(it.hasNext());
		assertEquals("Configured1", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Configured2", it.next().getName());
		assertFalse(it.hasNext());
	}

	public void testGetConfiguredPropertiesOfType() throws Exception {
		AnnotationBasedAnalyzerBeanDescriptor<MatchingAnalyzer> desc = AnnotationBasedAnalyzerBeanDescriptor
				.create(MatchingAnalyzer.class);

		Set<ConfiguredPropertyDescriptor> properties = desc.getConfiguredPropertiesByType(Number.class, false);
		assertEquals(0, properties.size());

		properties = desc.getConfiguredPropertiesByType(Number.class, true);
		assertEquals(0, properties.size());

		properties = desc.getConfiguredPropertiesByType(Dictionary.class, false);
		assertEquals(0, properties.size());

		properties = desc.getConfiguredPropertiesByType(Dictionary.class, true);
		assertEquals(1, properties.size());

		properties = desc.getConfiguredPropertiesByType(ReferenceData.class, false);
		assertEquals(0, properties.size());

		properties = desc.getConfiguredPropertiesByType(ReferenceData.class, true);
		assertEquals(2, properties.size());
	}

	public void testRowProcessingType() throws Exception {
		AnalyzerBeanDescriptor<RowProcessingAnalyzerMock> descriptor = AnnotationBasedAnalyzerBeanDescriptor
				.create(RowProcessingAnalyzerMock.class);
		assertEquals(false, descriptor.isExploringAnalyzer());
		assertEquals(true, descriptor.isRowProcessingAnalyzer());

		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
		Iterator<ConfiguredPropertyDescriptor> it = configuredProperties.iterator();
		assertTrue(it.hasNext());
		assertEquals("Columns", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Configured1", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Configured2", it.next().getName());
		assertFalse(it.hasNext());

		RowProcessingAnalyzerMock analyzerBean = new RowProcessingAnalyzerMock();
		ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty("Configured1");
		configuredProperty.setValue(analyzerBean, "foobar");
		assertEquals("foobar", analyzerBean.getConfigured1());
	}

	public void testGetInputDataTypeFamily() throws Exception {
		AnalyzerBeanDescriptor<?> descriptor = AnnotationBasedAnalyzerBeanDescriptor.create(StringAnalyzer.class);
		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredPropertiesForInput();
		assertEquals(1, configuredProperties.size());
		ConfiguredPropertyDescriptor propertyDescriptor = configuredProperties.iterator().next();

		assertEquals(DataTypeFamily.STRING, propertyDescriptor.getInputColumnDataTypeFamily());

		descriptor = AnnotationBasedAnalyzerBeanDescriptor.create(ValueDistributionAnalyzer.class);
		configuredProperties = descriptor.getConfiguredPropertiesForInput();
		assertEquals(1, configuredProperties.size());
		propertyDescriptor = configuredProperties.iterator().next();
		assertEquals(DataTypeFamily.UNDEFINED, propertyDescriptor.getInputColumnDataTypeFamily());
	}

	public void testAbstractBeanClass() throws Exception {
		try {
			AnnotationBasedAnalyzerBeanDescriptor.create(InvalidAnalyzer.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
					"Bean (class org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptorTest$InvalidAnalyzer) is not a non-abstract class",
					e.getMessage());
		}
	}

	@AnalyzerBean("invalid analyzer")
	public abstract class InvalidAnalyzer implements RowProcessingAnalyzer<AnalyzerResult> {
	}
}