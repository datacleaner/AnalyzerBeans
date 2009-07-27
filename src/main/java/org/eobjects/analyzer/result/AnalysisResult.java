package org.eobjects.analyzer.result;

import java.io.Serializable;
import java.util.List;

public interface AnalysisResult extends Serializable {

	public List<AnalyzerBeanResult> getAnalyzerBeanResults();
}
