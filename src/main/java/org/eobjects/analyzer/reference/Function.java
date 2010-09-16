package org.eobjects.analyzer.reference;

import java.io.Serializable;

public interface Function<I, O> extends Serializable {

	public O run(I input) throws Exception;
}
