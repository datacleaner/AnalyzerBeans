package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RunExplorerTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AnalyzerBeanInstance _analyzerBeanInstance;

	public RunExplorerTask(AnalyzerBeanInstance analyzerBeanInstance) {
		_analyzerBeanInstance = analyzerBeanInstance;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");
		_analyzerBeanInstance.run();
	}
}
