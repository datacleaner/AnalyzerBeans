package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.job.runner.RowProcessingPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RunRowProcessingPublisherTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RowProcessingPublisher _rowProcessingPublisher;

	public RunRowProcessingPublisherTask(RowProcessingPublisher rowProcessingPublisher) {
		_rowProcessingPublisher = rowProcessingPublisher;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");

		_rowProcessingPublisher.run();
	}

}
