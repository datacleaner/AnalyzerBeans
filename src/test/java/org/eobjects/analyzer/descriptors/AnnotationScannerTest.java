package org.eobjects.analyzer.descriptors;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.AnnotationScanner;

import junit.framework.TestCase;

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
				"org.eobjects.analyzer.descriptors", true);
		assertEquals(
				"{AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.descriptors.RowProcessingAnalyzerBean],AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.descriptors.ExploringAnalyzerBean]}",
				ArrayUtils.toString(analyzerDescriptors.toArray()));

		analyzerDescriptors = scanner.scanPackage(
				"org.eobjects.analyzer.descriptors", true);
		assertEquals(
				"{AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.descriptors.RowProcessingAnalyzerBean],AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.descriptors.ExploringAnalyzerBean]}",
				ArrayUtils.toString(analyzerDescriptors.toArray()));

		assertEquals(2, scanner.getDescriptors().size());
	}
}
