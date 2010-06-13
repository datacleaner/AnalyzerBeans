package org.eobjects.analyzer.job;

import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;

public class AssignAndInitializeRunnable implements Runnable {

	private AnalyzerBeanInstance analyzerBeanInstance;

	public AssignAndInitializeRunnable(AnalyzerBeanInstance analyzerBeanInstance) {
		this.analyzerBeanInstance = analyzerBeanInstance;
	}

	@Override
	public void run() {
		analyzerBeanInstance.assignConfigured();
		analyzerBeanInstance.assignProvided();
		analyzerBeanInstance.initialize();
	}

}
