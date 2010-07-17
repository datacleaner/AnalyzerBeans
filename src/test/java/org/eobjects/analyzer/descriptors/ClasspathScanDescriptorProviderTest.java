package org.eobjects.analyzer.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

public class ClasspathScanDescriptorProviderTest extends TestCase {

	public void testScanNonExistingPackage() throws Exception {
		Collection<AnalyzerBeanDescriptor> analyzerDescriptors = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.nonexistingbeans", true)
				.getDescriptors();
		assertEquals("{}", ArrayUtils.toString(analyzerDescriptors.toArray()));
	}

	public void testScanPackageRecursive() throws Exception {
		Collection<AnalyzerBeanDescriptor> analyzerDescriptors = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.beans.mock", true)
				.getDescriptors();
		List<AnalyzerBeanDescriptor> list = new ArrayList<AnalyzerBeanDescriptor>(
				analyzerDescriptors);
		Collections.sort(list);
		assertEquals(
				"{AnalyzerBeanDescriptor[beanClass=class org.eobjects.analyzer.beans.mock.ExploringBeanMock]," +
				"AnalyzerBeanDescriptor[beanClass=class org.eobjects.analyzer.beans.mock.RowProcessingBeanMock]}",
				ArrayUtils.toString(list.toArray()));

		analyzerDescriptors = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.descriptors", true)
				.getDescriptors();
		assertEquals(0, analyzerDescriptors.size());
	}
}
