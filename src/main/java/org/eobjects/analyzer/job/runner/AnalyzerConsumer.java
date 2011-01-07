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

import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

final class AnalyzerConsumer extends AbstractOutcomeSinkJobConsumer implements RowProcessingConsumer {

	private final AnalysisJob _job;
	private final AnalyzerJob _analyzerJob;
	private final AnalyzerBeanInstance _analyzerBeanInstance;
	private final InputColumn<?>[] _inputColumns;
	private final AnalysisListener _analysisListener;
	private final boolean _concurrent;

	public AnalyzerConsumer(AnalysisJob job, AnalyzerBeanInstance analyzerBeanInstance, AnalyzerJob analyzerJob,
			InputColumn<?>[] inputColumns, AnalysisListener analysisListener) {
		super(analyzerJob);
		_job = job;
		_analyzerBeanInstance = analyzerBeanInstance;
		_analyzerJob = analyzerJob;
		_inputColumns = inputColumns;
		_analysisListener = analysisListener;
		
		Concurrent concurrent = analyzerJob.getDescriptor().getAnnotation(Concurrent.class);
		if (concurrent == null) {
			// analyzers are by default not concurrent
			_concurrent = false;
		} else {
			_concurrent = concurrent.value();
		}
	}
	
	@Override
	public boolean isConcurrent() {
		return _concurrent;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount, OutcomeSink outcomes) {
		RowProcessingAnalyzer<?> analyzer = (RowProcessingAnalyzer<?>) _analyzerBeanInstance.getBean();
		try {
			analyzer.run(row, distinctCount);
		} catch (RuntimeException e) {
			_analysisListener.errorInAnalyzer(_job, _analyzerJob, e);
		}
		return row;
	}

	@Override
	public AnalyzerBeanInstance getBeanInstance() {
		return _analyzerBeanInstance;
	}

	@Override
	public AnalyzerJob getComponentJob() {
		return _analyzerJob;
	}

	@Override
	public String toString() {
		return "AnalyzerConsumer[" + _analyzerBeanInstance + "]";
	}
}
