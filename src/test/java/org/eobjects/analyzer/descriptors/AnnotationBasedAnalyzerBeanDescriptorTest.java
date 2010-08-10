package org.eobjects.analyzer.descriptors;

import java.util.Arrays;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock;
import org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.result.AnalyzerResult;

public class AnnotationBasedAnalyzerBeanDescriptorTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ExploringAnalyzerMock.clearInstances();
		RowProcessingAnalyzerMock.clearInstances();
	}

	public void testExploringType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnnotationBasedAnalyzerBeanDescriptor(
				ExploringAnalyzerMock.class);
		assertEquals(true, descriptor.isExploringAnalyzer());
		assertEquals(false, descriptor.isRowProcessingAnalyzer());

		assertEquals(
				"[ConfiguredPropertyDescriptorImpl[method=setConfigured2,baseType=class java.lang.Integer], ConfiguredPropertyDescriptorImpl[field=configured1,baseType=class java.lang.String]]",
				Arrays.toString(descriptor.getConfiguredProperties().toArray()));
	}

	public void testRowProcessingType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnnotationBasedAnalyzerBeanDescriptor(
				RowProcessingAnalyzerMock.class);
		assertEquals(false, descriptor.isExploringAnalyzer());
		assertEquals(true, descriptor.isRowProcessingAnalyzer());

		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor
				.getConfiguredProperties();
		assertEquals(
				"{ConfiguredPropertyDescriptorImpl[field=configured1,baseType=class java.lang.String],ConfiguredPropertyDescriptorImpl[method=setConfigured2,baseType=class java.lang.Integer],ConfiguredPropertyDescriptorImpl[field=columns,baseType=class [Lorg.eobjects.analyzer.data.InputColumn;]}",
				ArrayUtils.toString(configuredProperties.toArray()));

		RowProcessingAnalyzerMock analyzerBean = new RowProcessingAnalyzerMock();
		ConfiguredPropertyDescriptor configuredProperty = configuredProperties
				.iterator().next();
		configuredProperty.assignValue(analyzerBean, "foobar");
		assertEquals("foobar", analyzerBean.getConfigured1());
	}

	public void testGetInputDataTypeFamily() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnnotationBasedAnalyzerBeanDescriptor(
				StringAnalyzer.class);
		assertEquals(DataTypeFamily.STRING, descriptor.getInputDataTypeFamily());

		descriptor = new AnnotationBasedAnalyzerBeanDescriptor(
				ValueDistributionAnalyzer.class);
		assertEquals(DataTypeFamily.UNDEFINED,
				descriptor.getInputDataTypeFamily());
	}

	public void testAbstractBeanClass() throws Exception {
		try {
			new AnnotationBasedAnalyzerBeanDescriptor(InvalidAnalyzer.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
					"Bean (class org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptorTest$InvalidAnalyzer) is not a non-abstract class",
					e.getMessage());
		}
	}

	@AnalyzerBean("invalid analyzer")
	public abstract class InvalidAnalyzer implements
			RowProcessingAnalyzer<AnalyzerResult> {
	}
}