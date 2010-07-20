package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.job.runner.RowProcessingPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunRowProcessingPublisherTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private RowProcessingPublisher _rowProcessingPublisher;
	private CompletionListener _completionListener;

	public RunRowProcessingPublisherTask(
			RowProcessingPublisher rowProcessingPublisher,
			CompletionListener completionListener) {
		_rowProcessingPublisher = rowProcessingPublisher;
		_completionListener = completionListener;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");
		
		_rowProcessingPublisher.run();
		_completionListener.onComplete();
	}

}
