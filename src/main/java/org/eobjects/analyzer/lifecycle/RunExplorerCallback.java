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
package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;

public final class RunExplorerCallback implements AnalyzerLifeCycleCallback {

	private final AnalysisJob _job;
	private final AnalyzerJob _analyzerJob;
	private final DataContextProvider _dataContextProvider;
	private final AnalysisListener _analysisListener;

	public RunExplorerCallback(AnalysisJob job, AnalyzerJob analyzerJob, DataContextProvider dataContextProvider,
			AnalysisListener analysisListener) {
		_job = job;
		_analyzerJob = analyzerJob;
		_dataContextProvider = dataContextProvider;
		_analysisListener = analysisListener;
	}

	@Override
	public void onEvent(LifeCycleState state, Analyzer<?> analyzerBean, AnalyzerBeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isExploringAnalyzer();

		ExploringAnalyzer<?> exploringAnalyzer = (ExploringAnalyzer<?>) analyzerBean;
		if (_analysisListener != null) {
			_analysisListener.analyzerBegin(_job, _analyzerJob);
		}
		exploringAnalyzer.run(_dataContextProvider.getDataContext());
	}

}
