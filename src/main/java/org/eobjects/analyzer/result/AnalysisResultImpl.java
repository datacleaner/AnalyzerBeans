package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResultImpl implements AnalysisResult {

	private static final long serialVersionUID = 1L;

	private List<AnalyzerBeanResult> results = new ArrayList<AnalyzerBeanResult>();
	
	public void addResult(AnalyzerBeanResult result) {
		results.add(result);
	}
	
	@Override
	public List<AnalyzerBeanResult> getAnalyzerBeanResults() {
		return results;
	}

}
