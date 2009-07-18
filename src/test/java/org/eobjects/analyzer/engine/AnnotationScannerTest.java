package org.eobjects.analyzer.engine;

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
		List<AnalyzerBeanDescriptor> analyzerDescriptors = scanner.scanPackage("org.eobjects.analyzer.engine", true);
		assertEquals(
				"{AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.engine.RowProcessingAnalyzerBean],AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.engine.ExploringAnalyzerBean]}",
				ArrayUtils.toString(analyzerDescriptors.toArray()));

		analyzerDescriptors = scanner.scanPackage("org.eobjects.analyzer.engine", true);
		assertEquals(
				"{AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.engine.RowProcessingAnalyzerBean],AnalyzerBeanDescriptor[analyzerClass=class org.eobjects.analyzer.engine.ExploringAnalyzerBean]}",
				ArrayUtils.toString(analyzerDescriptors.toArray()));

		assertEquals(2, scanner.getDescriptors().size());
	}
}
