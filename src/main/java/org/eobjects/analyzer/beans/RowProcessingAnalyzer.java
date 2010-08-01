package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnalyzerResult;

public interface RowProcessingAnalyzer<R extends AnalyzerResult> extends Analyzer<R> {
	
	public void run(InputRow row, int distinctCount);
}
