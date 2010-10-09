package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseBeanTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AbstractBeanInstance<?> _beanInstance;

	public CloseBeanTask(AbstractBeanInstance<?> beanInstance) {
		_beanInstance = beanInstance;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");

		// close can occur AFTER completion
		_beanInstance.close();
	}
}
