package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CollectResultsAndCloseAnalyzerBeanTask extends CloseBeanTask {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AnalyzerBeanInstance analyzerBeanInstance;

	public CollectResultsAndCloseAnalyzerBeanTask(CompletionListener completionListener,
			AnalyzerBeanInstance analyzerBeanInstance) {
		super(completionListener, analyzerBeanInstance);
		this.analyzerBeanInstance = analyzerBeanInstance;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");
		analyzerBeanInstance.returnResults();

		super.execute();
	}

}
