package org.eobjects.analyzer.job.concurrent;

public final class PreviousErrorsExistException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public PreviousErrorsExistException(String message) {
		super(message);
	}

	public PreviousErrorsExistException() {
		super();
	}
}
