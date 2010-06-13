package org.eobjects.analyzer.job;

import java.util.List;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.DataContext;

/**
 * Represents an interface for the central execution mechanism in AnalyzerBeans.
 * AnalysisRunners have a life-cycle as follows:
 * 
 * <ol>
 * <li>addJob(AnalysisJob) is called any number of times (at least 1)</li>
 * <li>One and only one of the run(...) methods is called</li>
 * <li>The getResults() method can be called to retrieve the results after
 * execution</li>
 * </ol>
 * 
 * @author Kasper Sørensen
 * 
 */
public interface AnalysisRunner {

	public void addJob(AnalysisJob job);

	public void run(DataContextProvider dataContextProvider);

	public void run(DataContext dataContext);
	
	public boolean isDone();

	public List<AnalyzerResult> getResults();
}
