package org.eobjects.analyzer.descriptors;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

public class AnnotationScannerTest extends TestCase {

	public void testScanNonExistingPackage() throws Exception {
		AnnotationScanner scanner = new AnnotationScanner();
		List<AnalyzerBeanDescriptor> analyzerDescriptors = scanner.scanPackage(
				"org.eobjects.analyzer.nonexistingbeans", true);
		assertEquals("{}", ArrayUtils.toString(analyzerDescriptors.toArray()));
	}

	public void testScanPackageRecursive() throws Exception {
		AnnotationScanner scanner = new AnnotationScanner();
		List<AnalyzerBeanDescriptor> analyzerDescriptors = scanner.scanPackage(
				"org.eobjects.analyzer.beans.mock", true);
		Collections.sort(analyzerDescriptors);
		assertEquals(
				"{AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.beans.mock.ExploringBeanMock],AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.beans.mock.RowProcessingBeanMock]}",
				ArrayUtils.toString(analyzerDescriptors.toArray()));

		analyzerDescriptors = scanner.scanPackage(
				"org.eobjects.analyzer.descriptors", true);
		assertEquals(0, analyzerDescriptors.size());

		assertEquals(2, scanner.getDescriptors().size());
	}
}
