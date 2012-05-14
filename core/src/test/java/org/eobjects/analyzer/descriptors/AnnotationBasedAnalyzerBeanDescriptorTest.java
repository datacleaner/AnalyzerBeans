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

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.mock.AnalyzerMock;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ValueDistributionResult;

public class AnnotationBasedAnalyzerBeanDescriptorTest extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        AnalyzerMock.clearInstances();
    }

    public void testInheritedAnalyzer() throws Exception {
        AnalyzerBeanDescriptor<OneMoreMockAnalyzer> descriptor = Descriptors.ofAnalyzer(OneMoreMockAnalyzer.class);
        assertEquals("One more mock", descriptor.getDisplayName());
    }

    @AnalyzerBean("One more mock")
    public static class OneMoreMockAnalyzer extends AnalyzerMock {

    }

    public void testGetConfiguredPropertiesOfType() throws Exception {
        AnalyzerBeanDescriptor<AnalyzerMock> desc = Descriptors
                .ofAnalyzer(AnalyzerMock.class);

        Set<ConfiguredPropertyDescriptor> properties = desc.getConfiguredPropertiesByType(Number.class, false);
        assertEquals(0, properties.size());

        properties = desc.getConfiguredPropertiesByType(Number.class, true);
        assertEquals(0, properties.size());

        properties = desc.getConfiguredPropertiesByType(Dictionary.class, false);
        assertEquals(0, properties.size());

        properties = desc.getConfiguredPropertiesByType(String.class, true);
        assertEquals(1, properties.size());

        properties = desc.getConfiguredPropertiesByType(CharSequence.class, false);
        assertEquals(0, properties.size());

        properties = desc.getConfiguredPropertiesByType(CharSequence.class, true);
        assertEquals(1, properties.size());
    }

    public void testRowProcessingType() throws Exception {
        AnalyzerBeanDescriptor<AnalyzerMock> descriptor = Descriptors.ofAnalyzer(AnalyzerMock.class);

        Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
        Iterator<ConfiguredPropertyDescriptor> it = configuredProperties.iterator();
        assertTrue(it.hasNext());
        assertEquals("Columns", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Configured1", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Configured2", it.next().getName());
        assertFalse(it.hasNext());

        AnalyzerMock analyzerBean = new AnalyzerMock();
        ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty("Configured1");
        configuredProperty.setValue(analyzerBean, "foobar");
        assertEquals("foobar", analyzerBean.getConfigured1());
    }

    public void testGetResultMetrics() throws Exception {
        AnalyzerBeanDescriptor<?> descriptor = Descriptors.ofAnalyzer(ValueDistributionAnalyzer.class);
        assertEquals(ValueDistributionResult.class, descriptor.getResultClass());

        Set<MetricDescriptor> resultMetrics = descriptor.getResultMetrics();
        assertEquals("[MetricDescriptorImpl[name=Distinct count], " + "MetricDescriptorImpl[name=Null count], "
                + "MetricDescriptorImpl[name=Unique count], " + "MetricDescriptorImpl[name=Value count]]",
                resultMetrics.toString());

        MetricDescriptor metric = descriptor.getResultMetric("Unique count");
        assertEquals("MetricDescriptorImpl[name=Unique count]", metric.toString());
        assertFalse(metric.isParameterizedByInputColumn());
        assertFalse(metric.isParameterizedByString());

        metric = descriptor.getResultMetric("value count");
        assertNotNull(metric);
        assertEquals("MetricDescriptorImpl[name=Value count]", metric.toString());
        assertFalse(metric.isParameterizedByInputColumn());
        assertTrue(metric.isParameterizedByString());
    }

    public void testAbstractBeanClass() throws Exception {
        try {
            Descriptors.ofComponent(InvalidAnalyzer.class);
            fail("Exception expected");
        } catch (DescriptorException e) {
            assertEquals(
                    "Bean (class org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptorTest$InvalidAnalyzer) is not a non-abstract class",
                    e.getMessage());
        }
    }

    @AnalyzerBean("invalid analyzer")
    public abstract class InvalidAnalyzer implements Analyzer<AnalyzerResult> {
    }
}