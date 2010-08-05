package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunExplorerTask implements Task {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AnalyzerBeanInstance _analyzerBeanInstance;
	private final CompletionListener _completionListener;

	public RunExplorerTask(AnalyzerBeanInstance analyzerBeanInstance,
			CompletionListener completionListener) {
		_analyzerBeanInstance = analyzerBeanInstance;
		_completionListener = completionListener;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");
		
		_analyzerBeanInstance.run();
		_completionListener.onComplete();
	}

}
