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

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.concurrent.TaskRunner;

/**
 * Default implementation of the AnalysisRunner interface.
 * 
 * @author Kasper Sørensen
 */
public final class AnalysisRunnerImpl implements AnalysisRunner {

	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisListener[] _sharedAnalysisListeners;

	/**
	 * Creates an AnalysisRunner based on a configuration, with no listeners
	 * 
	 * @param configuration
	 */
	public AnalysisRunnerImpl(AnalyzerBeansConfiguration configuration) {
		this(configuration, new AnalysisListener[0]);
	}

	/**
	 * Create an AnalysisRunner with a set of listeners, based on a
	 * configuration
	 * 
	 * @param configuration
	 * @param sharedAnalysisListeners
	 */
	public AnalysisRunnerImpl(AnalyzerBeansConfiguration configuration, AnalysisListener... sharedAnalysisListeners) {
		if (configuration == null) {
			throw new IllegalArgumentException("configuration cannot be null");
		}
		_configuration = configuration;
		_sharedAnalysisListeners = sharedAnalysisListeners;
	}

	@Override
	public AnalysisResultFuture run(final AnalysisJob job) {
		final Queue<AnalyzerJobResult> resultQueue = new LinkedBlockingQueue<AnalyzerJobResult>();

		// This analysis listener will keep track of all collected errors
		final ErrorAwareAnalysisListener errorListener = new ErrorAwareAnalysisListener();

		// This analysis listener is a composite for all other listeners
		final CompositeAnalysisListener analysisListener = new CompositeAnalysisListener(errorListener,
				_sharedAnalysisListeners);

		if (DebugLoggingAnalysisListener.isEnabled()) {
			// enable debug logging?
			analysisListener.addDelegate(new DebugLoggingAnalysisListener());
		} else if (InfoLoggingAnalysisListener.isEnabled()) {
			analysisListener.addDelegate(new InfoLoggingAnalysisListener());
		}

		// set up the task runner that is aware of errors
		final TaskRunner taskRunner = new ErrorAwareTaskRunnerWrapper(errorListener, _configuration.getTaskRunner());

		// the delegate will do all the actual work
		final AnalysisRunnerJobDelegate delegate = new AnalysisRunnerJobDelegate(job, _configuration, taskRunner,
				analysisListener, resultQueue, errorListener);
		return delegate.run();
	}
}