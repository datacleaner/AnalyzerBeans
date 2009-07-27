package org.eobjects.analyzer.beans.mock;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Close;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.result.AnalyzerBeanResult;
import org.eobjects.analyzer.result.NumberResult;

import dk.eobjects.metamodel.DataContext;

@AnalyzerBean("Exploring mock")
public class ExploringBeanMock implements ExploringAnalyzer {

	private static List<ExploringBeanMock> instances = new LinkedList<ExploringBeanMock>();

	public static List<ExploringBeanMock> getInstances() {
		return instances;
	}

	public static void clearInstances() {
		instances.clear();
	}

	public ExploringBeanMock() {
		instances.add(this);
	}

	// A field-level @Configured property
	@Configured
	private String configured1;

	public String getConfigured1() {
		return configured1;
	}

	// A method-level @Configured property
	private Integer configured2;

	@Configured
	public void setConfigured2(Integer configured2) {
		this.configured2 = configured2;
	}

	public Integer getConfigured2() {
		return configured2;
	}

	// A field-level @Provided property
	@Provided
	private Map<String, Long> providedMap;

	public Map<String, Long> getProvidedMap() {
		return providedMap;
	}

	// A method-level @Provided property
	private List<Boolean> providedList;

	@Provided
	public void setProvidedList(List<Boolean> providedList) {
		this.providedList = providedList;
	}

	public List<Boolean> getProvidedList() {
		return providedList;
	}

	private boolean init1 = false;
	private boolean init2 = false;

	@Initialize
	public void init1() {
		this.init1 = true;
	}

	public boolean isInit1() {
		return init1;
	}

	@Initialize
	public void init2() {
		this.init2 = true;
	}

	public boolean isInit2() {
		return init2;
	}

	private int runCount;

	@Override
	public void run(DataContext dc) {
		TestCase.assertNotNull(dc);
		this.runCount++;
	}

	public int getRunCount() {
		return runCount;
	}

	private boolean close1 = false;
	private boolean close2 = false;

	@Close
	public void close1() {
		this.close1 = true;
	}

	public boolean isClose1() {
		return close1;
	}

	@Close
	public void close2() {
		this.close2 = true;
	}

	public boolean isClose2() {
		return close2;
	}

	private boolean result = false;

	@Result
	public AnalyzerBeanResult runCount() {
		result = true;
		return new NumberResult(getClass(), runCount);
	}

	public boolean isResult() {
		return result;
	}
}
