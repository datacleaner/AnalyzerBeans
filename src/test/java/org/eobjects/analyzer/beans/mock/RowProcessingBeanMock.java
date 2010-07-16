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
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.NumberResult;

@AnalyzerBean("Row-processing mock")
public class RowProcessingBeanMock implements RowProcessingAnalyzer {

	private static List<RowProcessingBeanMock> instances = new LinkedList<RowProcessingBeanMock>();

	public static List<RowProcessingBeanMock> getInstances() {
		return instances;
	}

	public static void clearInstances() {
		instances.clear();
	}

	public RowProcessingBeanMock() {
		instances.add(this);
	}

	@Configured
	private InputColumn<?>[] columns;

	public InputColumn<?>[] getColumns() {
		return columns;
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
	private long rowCount;

	@Override
	public void run(InputRow row, int count) {
		TestCase.assertNotNull(row);
		TestCase.assertNotNull(count);
		this.runCount++;
		this.rowCount += count;
	}

	public long getRowCount() {
		return rowCount;
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

	private boolean result1 = false;
	private boolean result2 = false;

	@Result("Row count")
	public AnalyzerResult rowCountResult() {
		result1 = true;
		return new NumberResult(getClass(), rowCount);
	}

	public boolean isResult1() {
		return result1;
	}

	@Result
	public AnalyzerResult runCount() {
		result2 = true;
		return new NumberResult(getClass(), runCount);
	}

	public boolean isResult2() {
		return result2;
	}
}