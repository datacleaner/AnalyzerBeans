package org.eobjects.analyzer.engine;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Require;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.annotations.Run;
import org.eobjects.analyzer.engine.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.engine.ExecutionType;
import org.eobjects.analyzer.engine.RequireDescriptor;
import org.eobjects.analyzer.result.AnalysisResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.Row;

public class AnalyzerBeanDescriptorTest extends TestCase {

	public void testExploringType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(ExploringAnalyser.class);
		assertEquals(ExecutionType.EXPLORING, descriptor.getExecutionType());
		assertEquals(true, descriptor.isExploringExecutionType());
		assertEquals(false, descriptor.isRowProcessingExecutionType());

		assertEquals(
				"{RequireDescriptor[method=null,field=public java.lang.String org.eobjects.analyzer.engine.ExploringAnalyser._configString],RequireDescriptor[method=public void org.eobjects.analyzer.engine.ExploringAnalyser.setBlabla(boolean),field=null]}",
				ArrayUtils.toString(descriptor.getRequireDescriptors().toArray()));
		assertEquals(
				"{RunDescriptor[method=public void org.eobjects.analyzer.engine.ExploringAnalyser.run(dk.eobjects.metamodel.DataContext)]}",
				ArrayUtils.toString(descriptor.getRunDescriptors().toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalysisResult org.eobjects.analyzer.engine.ExploringAnalyser.result1()],ResultDescriptor[method=public org.eobjects.analyzer.result.AnalysisResult org.eobjects.analyzer.engine.ExploringAnalyser.result2()]}",
				ArrayUtils.toString(descriptor.getResultDescriptors().toArray()));
	}

	public void testRowProcessingType() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(RowProcessingAnalyser.class);
		assertEquals(ExecutionType.ROW_PROCESSING, descriptor.getExecutionType());
		assertEquals(false, descriptor.isExploringExecutionType());
		assertEquals(true, descriptor.isRowProcessingExecutionType());

		List<RequireDescriptor> requireDescriptors = descriptor.getRequireDescriptors();
		assertEquals(
				"{RequireDescriptor[method=null,field=public java.lang.String org.eobjects.analyzer.engine.RowProcessingAnalyser._configString],RequireDescriptor[method=public void org.eobjects.analyzer.engine.RowProcessingAnalyser.setBlabla(boolean),field=null]}",
				ArrayUtils.toString(requireDescriptors.toArray()));
		assertEquals(
				"{RunDescriptor[method=public void org.eobjects.analyzer.engine.RowProcessingAnalyser.run(dk.eobjects.metamodel.data.Row,java.lang.Long)]}",
				ArrayUtils.toString(descriptor.getRunDescriptors().toArray()));
		assertEquals(
				"{ResultDescriptor[method=public org.eobjects.analyzer.result.AnalysisResult org.eobjects.analyzer.engine.RowProcessingAnalyser.result1()],ResultDescriptor[method=public org.eobjects.analyzer.result.AnalysisResult org.eobjects.analyzer.engine.RowProcessingAnalyser.result2()]}",
				ArrayUtils.toString(descriptor.getResultDescriptors().toArray()));

		RowProcessingAnalyser analyser = new RowProcessingAnalyser();
		RequireDescriptor requireDescriptor = requireDescriptors.get(0);
		requireDescriptor.assignValue(analyser, "foobar");
		assertEquals("foobar", analyser.getConfigString());

		requireDescriptor = requireDescriptors.get(1);
		requireDescriptor.assignValue(analyser, true);
		assertEquals("true", analyser.getConfigString());
	}
}

@AnalyzerBean(displayName = "Analyser mock-up", execution = ExecutionType.EXPLORING)
class ExploringAnalyser {

	@Require("config string")
	public String _configString;

	@Require("config bool")
	public void setBlabla(boolean bool) {
		_configString = Boolean.toString(bool);
	}

	@Run()
	public void run(DataContext dc) {
		System.out.println(_configString);
	}

	@Result("TableModel result")
	public AnalysisResult result1() {
		return null;
	}

	@Result("Row result")
	public AnalysisResult result2() {
		return null;
	}
}

@AnalyzerBean(displayName = "Analyser mock-up", execution = ExecutionType.ROW_PROCESSING)
class RowProcessingAnalyser {

	@Require("config string")
	public String _configString;

	@Require("config bool")
	public void setBlabla(boolean bool) {
		_configString = Boolean.toString(bool);
	}

	public String getConfigString() {
		return _configString;
	}

	@Run()
	public void run(Row row, Long count) {
		System.out.println(_configString);
	}

	@Result("TableModel result")
	public AnalysisResult result1() {
		return null;
	}

	@Result("Row result")
	public AnalysisResult result2() {
		return null;
	}
}