package org.eobjects.analyzer.descriptors;

/**
 * Exception thrown when an AnalyzerBean class does not conform to the
 * requirements imposed by the descriptors of such.
 */
public class DescriptorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DescriptorException() {
		super();
	}

	public DescriptorException(String message, Throwable cause) {
		super(message, cause);
	}

	public DescriptorException(String message) {
		super(message);
	}

	public DescriptorException(Throwable cause) {
		super(cause);
	}
}
