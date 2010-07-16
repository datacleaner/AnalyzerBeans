package org.eobjects.analyzer.data;

public interface InputRow {

	public <E> E getValue(InputColumn<E> column);
}
