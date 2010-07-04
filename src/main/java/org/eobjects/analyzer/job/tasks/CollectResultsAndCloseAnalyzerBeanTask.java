package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

public class CollectResultsAndCloseAnalyzerBeanTask implements Task {

	private AnalyzerBeanInstance analyzerBeanInstance;
	private CompletionListener completionListener;

	public CollectResultsAndCloseAnalyzerBeanTask(
			CompletionListener completionListener,
			AnalyzerBeanInstance analyzerBeanInstance) {
		this.completionListener = completionListener;
		this.analyzerBeanInstance = analyzerBeanInstance;
	}

	@Override
	public void execute() throws Exception {
		analyzerBeanInstance.returnResults();
		completionListener.onComplete();

		// close can occur AFTER completion
		analyzerBeanInstance.close();
	}

}
