/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job.runner;

import java.util.List;
import java.util.Map;
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
	 * Cancels the job, if it is still running.
	 */
	public void cancel();

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
	 * Finds (and waits if nescesary) the results of a single Analyzer.
	 * 
	 * @param analyzerJob
	 * @return the result for a given analyzer job
	 * @throws IllegalStateException
	 *             if the analysis did not go well (use isSuccesfull() or
	 *             isErrornous() to check)
	 */
	public AnalyzerResult getResult(AnalyzerJob analyzerJob) throws IllegalStateException;

	/**
	 * Finds (and waits if nescesary) the results mapped to the Analyzer jobs
	 * 
	 * @return a map with AnalyzerJobs as keys to the corresponding
	 *         AnalyzerResults.
	 * @throws IllegalStateException
	 *             if the analysis did not go well (use isSuccesfull() or
	 *             isErrornous() to check)
	 */
	public Map<AnalyzerJob, AnalyzerResult> getResultMap() throws IllegalStateException;

	/**
	 * @return any errors reported during execution, if the job was not
	 *         successful
	 */
	@Override
	public List<Throwable> getErrors();
}