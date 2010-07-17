package org.eobjects.analyzer.job;

import java.util.Collection;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.data.InputColumn;

public interface AnalysisJob {

	public DataContextProvider getDataContextProvider();
	
	public Collection<InputColumn<?>> getSourceColumns();
	
	public Collection<TransformerJob> getTransformerJobs();
	
	public Collection<AnalyzerJob> getAnalyzerJobs();
}
