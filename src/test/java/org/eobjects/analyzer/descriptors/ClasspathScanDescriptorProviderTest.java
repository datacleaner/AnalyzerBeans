package org.eobjects.analyzer.descriptors;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class ClasspathScanDescriptorProviderTest extends TestCase {

	public void testScanNonExistingPackage() throws Exception {
		ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider();
		Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = provider
				.scanPackage("org.eobjects.analyzer.nonexistingbeans", true)
				.getAnalyzerBeanDescriptors();
		assertEquals("[]", Arrays.toString(analyzerDescriptors.toArray()));

		assertEquals("[]", provider.getTransformerBeanDescriptors().toString());
		assertEquals("[]", provider.getRendererBeanDescriptors().toString());
	}

	public void testScanPackageRecursive() throws Exception {
		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider();
		Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = descriptorProvider
				.scanPackage("org.eobjects.analyzer.beans.mock", true)
				.getAnalyzerBeanDescriptors();
		Object[] array = analyzerDescriptors.toArray();
		Arrays.sort(array);
		assertEquals(
				"[AnnotationBasedAnalyzerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock], AnnotationBasedAnalyzerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock]]",
				Arrays.toString(array));

		Collection<TransformerBeanDescriptor<?>> transformerBeanDescriptors = descriptorProvider
				.getTransformerBeanDescriptors();
		assertEquals(
				"[AnnotationBasedTransformerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.mock.TransformerMock]]",
				Arrays.toString(transformerBeanDescriptors.toArray()));

		analyzerDescriptors = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.job", true)
				.getAnalyzerBeanDescriptors();
		assertEquals(0, analyzerDescriptors.size());
	}

	public void testScanRenderers() throws Exception {
		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider();
		Collection<RendererBeanDescriptor> rendererBeanDescriptors = descriptorProvider
				.scanPackage("org.eobjects.analyzer.result.renderer", true)
				.getRendererBeanDescriptors();
		assertEquals(
				"[AnnotationBasedRendererBeanDescriptor[beanClass=org.eobjects.analyzer.result.renderer.DefaultTextRenderer]]",
				rendererBeanDescriptors.toString());
	}
}
