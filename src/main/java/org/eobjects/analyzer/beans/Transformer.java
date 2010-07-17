package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputRow;

public interface Transformer<E> {

	public int getOutputColumns();

	public E[] transform(InputRow inputRow);
}