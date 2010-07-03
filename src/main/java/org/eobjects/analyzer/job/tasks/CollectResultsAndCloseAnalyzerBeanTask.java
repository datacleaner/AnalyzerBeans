package org.eobjects.analyzer.job.tasks;

import java.util.concurrent.Callable;

import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

public class CollectResultsAndCloseAnalyzerBeanTask implements Callable<Object> {

	private AnalyzerBeanInstance analyzerBeanInstance;
	private CompletionListener completionListener;

	public CollectResultsAndCloseAnalyzerBeanTask(
			CompletionListener completionListener,
			AnalyzerBeanInstance analyzerBeanInstance) {
		this.completionListener = completionListener;
		this.analyzerBeanInstance = analyzerBeanInstance;
	}

	@Override
	public Object call() throws Exception {
		analyzerBeanInstance.returnResults();
		completionListener.onComplete();

		// close can occur AFTER completion
		analyzerBeanInstance.close();
		return Boolean.TRUE;
	}

}
