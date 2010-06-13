package org.eobjects.analyzer.job;

import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

class CollectResultsAndCloseAnalyzerBeanRunnable implements Runnable {

	private AnalyzerBeanInstance analyzerBeanInstance;

	public CollectResultsAndCloseAnalyzerBeanRunnable(AnalyzerBeanInstance analyzerBeanInstance) {
		this.analyzerBeanInstance = analyzerBeanInstance;
	}

	@Override
	public void run() {
		analyzerBeanInstance.returnResults();
		analyzerBeanInstance.close();
	}

}
