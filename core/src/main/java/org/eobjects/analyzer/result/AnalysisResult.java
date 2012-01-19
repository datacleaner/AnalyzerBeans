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
package org.eobjects.analyzer.result;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;

/**
 * Represents a collection of results for an {@link AnalysisJob}.
 * 
 * @author Kasper Sørensen
 */
public interface AnalysisResult {

	/**
	 * Gets the the results of this analysis.
	 * 
	 * @return the results from the Analyzers in the executed job
	 */
	public List<AnalyzerResult> getResults();

	/**
	 * Gets the results of a single Analyzer.
	 * 
	 * @param componentJob
	 *            the component (either analyzer or explorer) job to find the
	 *            result for
	 * @return the result for a given component job
	 */
	public AnalyzerResult getResult(ComponentJob componentJob);

	/**
	 * Gets the results mapped to the Component jobs
	 * 
	 * @return a map with ComponentJobs as keys to the corresponding
	 *         AnalyzerResults.
	 */
	public Map<ComponentJob, AnalyzerResult> getResultMap();

	/**
	 * Gets the time that the results were created
	 * 
	 * @return the time that the results were created
	 */
	public Date getCreationDate();
}
