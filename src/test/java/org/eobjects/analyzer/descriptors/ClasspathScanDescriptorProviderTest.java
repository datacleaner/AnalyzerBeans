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

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;

import junit.framework.TestCase;

public class ClasspathScanDescriptorProviderTest extends TestCase {

	private MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(2);

	public void testScanNonExistingPackage() throws Exception {
		ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
		Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = provider.scanPackage(
				"org.eobjects.analyzer.nonexistingbeans", true).getAnalyzerBeanDescriptors();
		assertEquals("[]", Arrays.toString(analyzerDescriptors.toArray()));

		assertEquals("[]", provider.getTransformerBeanDescriptors().toString());
		assertEquals("[]", provider.getRendererBeanDescriptors().toString());
	}

	public void testScanPackageRecursive() throws Exception {
		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
		Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = descriptorProvider.scanPackage(
				"org.eobjects.analyzer.beans.mock", true).getAnalyzerBeanDescriptors();
		Object[] array = analyzerDescriptors.toArray();
		Arrays.sort(array);
		assertEquals("[AnnotationBasedAnalyzerBeanDescriptor[org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock],"
				+ " AnnotationBasedAnalyzerBeanDescriptor[org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock]]",
				Arrays.toString(array));

		Collection<TransformerBeanDescriptor<?>> transformerBeanDescriptors = descriptorProvider
				.getTransformerBeanDescriptors();
		assertEquals("[AnnotationBasedTransformerBeanDescriptor[org.eobjects.analyzer.beans.mock.TransformerMock]]",
				Arrays.toString(transformerBeanDescriptors.toArray()));

		analyzerDescriptors = new ClasspathScanDescriptorProvider(taskRunner).scanPackage("org.eobjects.analyzer.job", true)
				.getAnalyzerBeanDescriptors();
		assertEquals(0, analyzerDescriptors.size());
	}

	public void testScanRenderers() throws Exception {
		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
		Collection<RendererBeanDescriptor> rendererBeanDescriptors = descriptorProvider.scanPackage(
				"org.eobjects.analyzer.result.renderer", true).getRendererBeanDescriptors();
		assertEquals("[AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.CrosstabTextRenderer], "
				+ "AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.DateGapTextRenderer], "
				+ "AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.DefaultTextRenderer]]",
				new TreeSet<RendererBeanDescriptor>(rendererBeanDescriptors).toString());
	}
}
