package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptor;

import junit.framework.TestCase;

public class DateGapAnalyzerTest extends TestCase {

	public void testDescriptor() throws Exception {
		AnnotationBasedAnalyzerBeanDescriptor<DateGapAnalyzer> descriptor = AnnotationBasedAnalyzerBeanDescriptor
				.create(DateGapAnalyzer.class);
		assertEquals("Date gap analyzer", descriptor.getDisplayName());
	}
}
