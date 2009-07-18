package org.eobjects.analyzer.descriptors;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.ExecutionType;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.annotations.Run;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.result.AnalyzerBeanResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.Row;

public class AnalyzerBeanDescriptorTest extends TestCase {

	public void testExploringType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				ExploringAnalyzerBean.class);
		assertEquals(ExecutionType.EXPLORING, descriptor.getExecutionType());
		assertEquals(true, descriptor.isExploringExecutionType());
		assertEquals(false, descriptor.isRowProcessingExecutionType());

		assertEquals(
				"{ConfiguredDescriptor[method=null,field=public java.lang.String org.eobjects.analyzer.engine.ExploringAnalyzerBean.configString],ConfiguredDescriptor[method=public void org.eobjects.analyzer.engine.ExploringAnalyzerBean.setBlabla(boolean),field=null]}",
				ArrayUtils.toString(descriptor.getConfiguredDescriptors()
						.toArray()));
		assertEquals(
				"{RunDescriptor[method=public void org.eobjects.analyzer.engine.ExploringAnalyzerBean.run(dk.eobjects.metamodel.DataContext)]}",
				ArrayUtils.toString(descriptor.getRunDescriptors().toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerBeanResult org.eobjects.analyzer.engine.ExploringAnalyzerBean.result1()],ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerBeanResult org.eobjects.analyzer.engine.ExploringAnalyzerBean.result2()]}",
				ArrayUtils
						.toString(descriptor.getResultDescriptors().toArray()));
	}

	public void testRowProcessingType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				RowProcessingAnalyzerBean.class);
		assertEquals(ExecutionType.ROW_PROCESSING, descriptor
				.getExecutionType());
		assertEquals(false, descriptor.isExploringExecutionType());
		assertEquals(true, descriptor.isRowProcessingExecutionType());

		List<ConfiguredDescriptor> configuredDescriptors = descriptor
				.getConfiguredDescriptors();
		assertEquals(
				"{ConfiguredDescriptor[method=null,field=public java.lang.String org.eobjects.analyzer.engine.RowProcessingAnalyzerBean.configString],ConfiguredDescriptor[method=public void org.eobjects.analyzer.engine.RowProcessingAnalyzerBean.setBlabla(boolean),field=null]}",
				ArrayUtils.toString(configuredDescriptors.toArray()));
		assertEquals(
				"{RunDescriptor[method=public void org.eobjects.analyzer.engine.RowProcessingAnalyzerBean.run(dk.eobjects.metamodel.data.Row,java.lang.Long)]}",
				ArrayUtils.toString(descriptor.getRunDescriptors().toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerBeanResult org.eobjects.analyzer.engine.RowProcessingAnalyzerBean.result1()],ResultDescriptor[method=public org.eobjects.analyzer.result.AnalyzerBeanResult org.eobjects.analyzer.engine.RowProcessingAnalyzerBean.result2()]}",
				ArrayUtils
						.toString(descriptor.getResultDescriptors().toArray()));

		RowProcessingAnalyzerBean analyzerBean = new RowProcessingAnalyzerBean();
		ConfiguredDescriptor configuredDescriptor = configuredDescriptors
				.get(0);
		configuredDescriptor.assignValue(analyzerBean, "foobar");
		assertEquals("foobar", analyzerBean.getConfigString());

		configuredDescriptor = configuredDescriptors.get(1);
		configuredDescriptor.assignValue(analyzerBean, true);
		assertEquals("true", analyzerBean.getConfigString());
	}
}

@AnalyzerBean(displayName = "AnalyzerBean mock-up", execution = ExecutionType.EXPLORING)
class ExploringAnalyzerBean {

	@Configured("config string")
	public String configString;

	@Configured("config bool")
	public void setBlabla(boolean bool) {
		configString = Boolean.toString(bool);
	}

	@Run()
	public void run(DataContext dc) {
		System.out.println(configString);
	}

	@Result("TableModel result")
	public AnalyzerBeanResult result1() {
		return null;
	}

	@Result("Row result")
	public AnalyzerBeanResult result2() {
		return null;
	}
}

@AnalyzerBean(displayName = "AnalyzerBean mock-up", execution = ExecutionType.ROW_PROCESSING)
class RowProcessingAnalyzerBean {

	@Configured("config string")
	public String configString;

	@Configured("config bool")
	public void setBlabla(boolean bool) {
		configString = Boolean.toString(bool);
	}

	public String getConfigString() {
		return configString;
	}

	@Run()
	public void run(Row row, Long count) {
		System.out.println(configString);
	}

	@Result("TableModel result")
	public AnalyzerBeanResult result1() {
		return null;
	}

	@Result("Row result")
	public AnalyzerBeanResult result2() {
		return null;
	}
}