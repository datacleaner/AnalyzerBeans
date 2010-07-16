package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

public interface Transformer<E> {

	public InputColumn<E>[] getVirtualInputColumns();

	public E[] transform(InputRow inputRow);
}