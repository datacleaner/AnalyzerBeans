package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputRow;

public interface RowProcessingAnalyzer extends Analyzer {
	
	public void run(InputRow row, int distinctCount);
}
