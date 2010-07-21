package org.eobjects.analyzer.descriptors;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock;
import org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.data.DataTypeFamily;

public class AnalyzerBeanDescriptorTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ExploringAnalyzerMock.clearInstances();
		RowProcessingAnalyzerMock.clearInstances();
	}

	public void testExploringType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				ExploringAnalyzerMock.class);
		assertEquals(true, descriptor.isExploringAnalyzer());
		assertEquals(false, descriptor.isRowProcessingAnalyzer());

		assertEquals(
				"{ConfiguredDescriptor[method=null,field=private java.lang.String org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock.configured1],ConfiguredDescriptor[method=public void org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock.setConfigured2(java.lang.Integer),field=null]}",
				ArrayUtils.toString(descriptor.getConfiguredDescriptors()
						.toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerResult org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock.runCount()]}",
				ArrayUtils
						.toString(descriptor.getResultDescriptors().toArray()));
	}

	public void testRowProcessingType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				RowProcessingAnalyzerMock.class);
		assertEquals(false, descriptor.isExploringAnalyzer());
		assertEquals(true, descriptor.isRowProcessingAnalyzer());

		List<ConfiguredDescriptor> configuredDescriptors = descriptor
				.getConfiguredDescriptors();
		assertEquals(
				"{ConfiguredDescriptor[method=null,field=private org.eobjects.analyzer.data.InputColumn[] org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock.columns],"
						+ "ConfiguredDescriptor[method=null,field=private java.lang.String org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock.configured1],"
						+ "ConfiguredDescriptor[method=public void org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock.setConfigured2(java.lang.Integer),field=null]}",
				ArrayUtils.toString(configuredDescriptors.toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerResult org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock.runCount()],ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerResult org.eobjects.analyzer.beans.mock.RowProcessingAnalyzerMock.rowCountResult()]}",
				ArrayUtils
						.toString(descriptor.getResultDescriptors().toArray()));

		RowProcessingAnalyzerMock analyzerBean = new RowProcessingAnalyzerMock();
		ConfiguredDescriptor configuredDescriptor = configuredDescriptors
				.get(1);
		configuredDescriptor.assignValue(analyzerBean, "foobar");
		assertEquals("foobar", analyzerBean.getConfigured1());
	}

	public void testGetInputDataTypeFamily() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				StringAnalyzer.class);
		assertEquals(DataTypeFamily.STRING, descriptor.getInputDataTypeFamily());

		descriptor = new AnalyzerBeanDescriptor(ValueDistributionAnalyzer.class);
		assertEquals(DataTypeFamily.UNDEFINED,
				descriptor.getInputDataTypeFamily());
	}
}