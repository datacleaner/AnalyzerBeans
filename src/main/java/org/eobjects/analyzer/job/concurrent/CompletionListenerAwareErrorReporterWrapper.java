package org.eobjects.analyzer.job.concurrent;

public final class CompletionListenerAwareErrorReporterWrapper implements ErrorReporter {

	private final CompletionListener _completionListener;
	private final ErrorReporter _delegate;

	public CompletionListenerAwareErrorReporterWrapper(CompletionListener completionListener, ErrorReporter delegate) {
		_completionListener = completionListener;
		_delegate = delegate;
	}

	@Override
	public void reportError(Throwable throwable) {
		_delegate.reportError(throwable);
		_completionListener.onComplete();
	}

}
