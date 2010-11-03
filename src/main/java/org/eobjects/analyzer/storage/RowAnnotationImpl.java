package org.eobjects.analyzer.storage;

final class RowAnnotationImpl implements RowAnnotation {

	private volatile int _rowCount;
	
	protected void incrementRowCount(int increment) {
		_rowCount += increment;
	}
	
	protected void resetRowCount() {
		_rowCount = 0;
	}

	@Override
	public int getRowCount() {
		return _rowCount;
	}

}
