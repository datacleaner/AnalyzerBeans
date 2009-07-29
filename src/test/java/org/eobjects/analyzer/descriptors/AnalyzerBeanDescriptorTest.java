package org.eobjects.analyzer.descriptors;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.beans.mock.ExploringBeanMock;
import org.eobjects.analyzer.beans.mock.RowProcessingBeanMock;

public class AnalyzerBeanDescriptorTest extends TestCase {
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ExploringBeanMock.clearInstances();
		RowProcessingBeanMock.clearInstances();
	}

	public void testExploringType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				ExploringBeanMock.class);
		assertEquals(true, descriptor.isExploringAnalyzer());
		assertEquals(false, descriptor.isRowProcessingAnalyzer());

		assertEquals(
				"{ConfiguredDescriptor[method=null,field=private java.lang.String org.eobjects.analyzer.beans.mock.ExploringBeanMock.configured1],ConfiguredDescriptor[method=public void org.eobjects.analyzer.beans.mock.ExploringBeanMock.setConfigured2(java.lang.Integer),field=null]}",
				ArrayUtils.toString(descriptor.getConfiguredDescriptors()
						.toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerResult org.eobjects.analyzer.beans.mock.ExploringBeanMock.runCount()]}",
				ArrayUtils
						.toString(descriptor.getResultDescriptors().toArray()));
	}

	public void testRowProcessingType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				RowProcessingBeanMock.class);
		assertEquals(false, descriptor.isExploringAnalyzer());
		assertEquals(true, descriptor.isRowProcessingAnalyzer());

		List<ConfiguredDescriptor> configuredDescriptors = descriptor
				.getConfiguredDescriptors();
		assertEquals(
				"{ConfiguredDescriptor[method=null,field=private dk.eobjects.metamodel.schema.Column[] org.eobjects.analyzer.beans.mock.RowProcessingBeanMock.columns]," +
				"ConfiguredDescriptor[method=null,field=private java.lang.String org.eobjects.analyzer.beans.mock.RowProcessingBeanMock.configured1]," +
				"ConfiguredDescriptor[method=public void org.eobjects.analyzer.beans.mock.RowProcessingBeanMock.setConfigured2(java.lang.Integer),field=null]}",
				ArrayUtils.toString(configuredDescriptors.toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerResult org.eobjects.analyzer.beans.mock.RowProcessingBeanMock.runCount()],ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerResult org.eobjects.analyzer.beans.mock.RowProcessingBeanMock.rowCountResult()]}",
				ArrayUtils
						.toString(descriptor.getResultDescriptors().toArray()));

		RowProcessingBeanMock analyzerBean = new RowProcessingBeanMock();
		ConfiguredDescriptor configuredDescriptor = configuredDescriptors
				.get(1);
		configuredDescriptor.assignValue(analyzerBean, "foobar");
		assertEquals("foobar", analyzerBean.getConfigured1());
	}
}