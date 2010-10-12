package org.eobjects.analyzer.beans.mock;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.NumberResult;

@AnalyzerBean("Row-processing mock")
public class RowProcessingAnalyzerMock implements RowProcessingAnalyzer<NumberResult> {

	private static List<RowProcessingAnalyzerMock> instances = new LinkedList<RowProcessingAnalyzerMock>();

	public static List<RowProcessingAnalyzerMock> getInstances() {
		return instances;
	}

	public static void clearInstances() {
		instances.clear();
	}

	public RowProcessingAnalyzerMock() {
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

	@Configured
	private Integer configured2;

	public Integer getConfigured2() {
		return configured2;
	}

	// A field-level @Provided property
	@Provided
	private Map<String, Long> providedMap;

	public Map<String, Long> getProvidedMap() {
		return providedMap;
	}

	@Provided
	private List<Boolean> providedList;

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

	public NumberResult getResult() {
		return new NumberResult( rowCount);
	}
}