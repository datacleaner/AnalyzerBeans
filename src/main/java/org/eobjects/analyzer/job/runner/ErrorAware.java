package org.eobjects.analyzer.job.runner;

import java.util.List;

public interface ErrorAware {

	public boolean isErrornous();

	public List<Throwable> getErrors();
}
