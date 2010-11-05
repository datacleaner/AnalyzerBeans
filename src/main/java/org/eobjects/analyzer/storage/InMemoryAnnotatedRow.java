package org.eobjects.analyzer.storage;

import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * InputRow implementation used by the InMemoryRowAnnotationFactory to capture
 * the distinct counts of a row
 * 
 * @author Kasper Sørensen
 */
final class InMemoryAnnotatedRow implements InputRow {

	private final InputRow _delegate;
	private final int _distinctCount;

	public InMemoryAnnotatedRow(InputRow delegate, int distinctCount) {
		_delegate = delegate;
		_distinctCount = distinctCount;
	}

	@Override
	public <E> E getValue(InputColumn<E> column) {
		return _delegate.getValue(column);
	}

	@Override
	public int getId() {
		return _delegate.getId();
	}

	@Override
	public List<InputColumn<?>> getInputColumns() {
		return _delegate.getInputColumns();
	}

	public int getDistinctCount() {
		return _distinctCount;
	}

	@Override
	public String toString() {
		return _delegate.toString();
	}
	
	public InputRow getDelegate() {
		return _delegate;
	}
}
