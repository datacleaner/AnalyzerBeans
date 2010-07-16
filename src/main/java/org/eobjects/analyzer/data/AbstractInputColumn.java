package org.eobjects.analyzer.data;

import dk.eobjects.metamodel.schema.Column;

public abstract class AbstractInputColumn<E> implements InputColumn<E> {

	@Override
	public boolean isPhysicalColumn() {
		return getPhysicalColumnInternal() != null;
	}

	@Override
	public boolean isVirtualColumn() {
		return getPhysicalColumnInternal() == null;
	}

	@Override
	public Column getPhysicalColumn() throws IllegalStateException {
		if (!isPhysicalColumn()) {
			throw new IllegalStateException(this
					+ " is not a physical InputColumn");
		}
		return getPhysicalColumnInternal();
	}

	protected abstract Column getPhysicalColumnInternal();

	protected abstract int hashCodeInternal();

	protected abstract boolean equalsInternal(AbstractInputColumn<?> that);

	@Override
	public int compareTo(InputColumn<E> o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public int hashCode() {
		return hashCodeInternal();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() == this.getClass()) {
			AbstractInputColumn<?> that = (AbstractInputColumn<?>) obj;
			if (that.isPhysicalColumn() == this.isPhysicalColumn()) {
				return equalsInternal(that);
			}
		}
		return false;
	}
}
