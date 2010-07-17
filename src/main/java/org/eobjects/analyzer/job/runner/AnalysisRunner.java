package org.eobjects.analyzer.job.runner;

import java.util.Collection;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.job.SimpleAnalyzerJob;

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

	public AnalysisResultFuture run(DataContextProvider dataContextProvider,
			Collection<? extends SimpleAnalyzerJob> jobs);

	public AnalysisResultFuture run(DataContextProvider dataContextProvider,
			SimpleAnalyzerJob firstJob, SimpleAnalyzerJob... additionalJobs);
}
