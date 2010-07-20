package org.eobjects.analyzer.descriptors;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class ClasspathScanDescriptorProviderTest extends TestCase {

	public void testScanNonExistingPackage() throws Exception {
		Collection<AnalyzerBeanDescriptor> analyzerDescriptors = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.nonexistingbeans", true)
				.getAnalyzerBeanDescriptors();
		assertEquals("[]", Arrays.toString(analyzerDescriptors.toArray()));
	}

	public void testScanPackageRecursive() throws Exception {
		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider();
		Collection<AnalyzerBeanDescriptor> analyzerDescriptors = descriptorProvider
				.scanPackage("org.eobjects.analyzer.beans.mock", true)
				.getAnalyzerBeanDescriptors();
		Object[] array = analyzerDescriptors.toArray();
		Arrays.sort(array);
		assertEquals(
				"[AnalyzerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.mock.ExploringBeanMock], "
						+ "AnalyzerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.mock.RowProcessingBeanMock]]",
				Arrays.toString(array));

		Collection<TransformerBeanDescriptor> transformerBeanDescriptors = descriptorProvider
				.getTransformerBeanDescriptors();
		assertEquals(
				"[TransformerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.mock.TransformerMock]]",
				Arrays.toString(transformerBeanDescriptors.toArray()));

		analyzerDescriptors = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.descriptors", true)
				.getAnalyzerBeanDescriptors();
		assertEquals(0, analyzerDescriptors.size());
	}
}
