package org.eobjects.analyzer.job.runner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * Represents the result of an analysis. The analysis may still be running,
 * which is why this interface contains the isDone(), await() and
 * await(long,TimeUnit) methods.
 * 
 * When the result is done it may either be successful or errornous. Clients can
 * find out using the isSuccessful() or isErrornous() methods.
 * 
 * If succesful, the results can be retrieved using the getResults() method. If
 * errornous the error messages can be retrieved using the getErrors() method.
 * If the analysis was only partly errornous, there may be both result and
 * errors, but isSuccesful() will return false.
 * 
 * @author Kasper Sørensen
 */
public interface AnalysisResultFuture extends ErrorAware {

	/**
	 * @return true if the job is still executing
	 */
	public boolean isDone();

	/**
	 * Blocks the current thread until interrupted, most probably because the
	 * job has ended.
	 */
	public void await();

	/**
	 * Blocks the current thread until interrupted, either because the job has
	 * ended or because it has timed out.
	 * 
	 * @param timeout
	 * @param timeUnit
	 */
	public void await(long timeout, TimeUnit timeUnit);

	/**
	 * @return true if the job executed without errors
	 */
	public boolean isSuccessful();

	/**
	 * @return SUCCESSFUL if the job is finished and successful, ERRORNOUS if
	 *         errors have been reported and NOT_FINISHED if no errors have been
	 *         reported but the job is not done yet
	 */
	public JobStatus getStatus();

	/**
	 * Finds (and waits if nescesary) the results of this analysis.
	 * 
	 * @return the results from the Analyzers in the executed job
	 * @throws IllegalStateException
	 *             if the analysis did not go well (use isSuccesfull() or
	 *             isErrornous() to check)
	 */
	public List<AnalyzerResult> getResults() throws IllegalStateException;

	/**
	 * Finds (and waits if nescesary) the results of a single analyzer.
	 * 
	 * @param analyzerJob
	 * @return the result for a given analyzer job
	 * @throws IllegalStateException
	 *             if the analysis did not go well (use isSuccesfull() or
	 *             isErrornous() to check)
	 */
	public AnalyzerResult getResult(AnalyzerJob analyzerJob);

	/**
	 * @return any errors reported during execution, if the job was not
	 *         successful
	 */
	@Override
	public List<Throwable> getErrors();
}
