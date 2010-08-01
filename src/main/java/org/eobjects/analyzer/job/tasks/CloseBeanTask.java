package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseBeanTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AbstractBeanInstance<?> _beanInstance;
	private CompletionListener _completionListener;

	public CloseBeanTask(CompletionListener completionListener,
			AbstractBeanInstance<?> beanInstance) {
		_beanInstance = beanInstance;
		_completionListener = completionListener;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");
		_completionListener.onComplete();

		// close can occur AFTER completion
		_beanInstance.close();
	}
}
