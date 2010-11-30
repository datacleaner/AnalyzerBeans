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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Table;

/**
 * AnalysisListener that will register errors
 * 
 * @author Kasper Sørensen
 */
final class ErrorAwareAnalysisListener implements AnalysisListener, ErrorAware {

	private static final Logger logger = LoggerFactory.getLogger(ErrorAwareAnalysisListener.class);

	private final List<Throwable> _errors = new LinkedList<Throwable>();

	@Override
	public void jobBegin(AnalysisJob job) {
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
	}

	private void storeError(AnalysisJob job, Throwable throwable) {
		synchronized (_errors) {
			if (!_errors.contains(throwable)) {
				_errors.add(throwable);
			}

		}
	}

	@Override
	public List<Throwable> getErrors() {
		return Collections.synchronizedList(Collections.unmodifiableList(_errors));
	}

	@Override
	public boolean isErrornous() {
		synchronized (_errors) {
			return !_errors.isEmpty();
		}
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
		logger.warn("errorInFilter({},{},{})", new Object[] { job, filterJob, throwable });
		storeError(job, throwable);
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
		logger.warn("errorInTransformer({},{},{})", new Object[] { job, transformerJob, throwable });
		storeError(job, throwable);
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
		logger.warn("errorInAnalyzer({},{},{})", new Object[] { job, analyzerJob, throwable });
		storeError(job, throwable);
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
		logger.warn("errorUnknown({},{})", new Object[] { job, throwable });
		logger.warn("Exception stack trace:", throwable);
		storeError(job, throwable);
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows) {
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, Table table) {
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob) {
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
	}
}
