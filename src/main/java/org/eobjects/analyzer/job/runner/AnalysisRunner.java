package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;

/**
 * Central component for executing/running AnalysisJobs. Typically an
 * AnalysisRunner will do all the complicated work of traversing the
 * AnalysisJob, setting up filters, transformers and analyzers and kick off row
 * processing.
 * 
 * Typeically an AnalysisRunner will be able to utilize multithreading and will
 * therefore be able to return much earlier than when the job is finished.
 * Therefore the result of the run(...) method is a <i>Future</i>, which means
 * that it is a reference to a future result. You can use the future to ask if
 * the result is ready or it is possible to wait/block untill it is done.
 * 
 * @author Kasper Sørensen
 */
public interface AnalysisRunner {

	public AnalysisResultFuture run(AnalysisJob job);
}
