package org.eobjects.analyzer.data;

import java.io.Serializable;

import org.eobjects.analyzer.job.IdGenerator;

import dk.eobjects.metamodel.schema.Column;

public class TransformedInputColumn<E> extends AbstractInputColumn<E>
		implements MutableInputColumn<E>, Serializable {

	private static final long serialVersionUID = 1L;

	private String _id;
	private String _name;

	public TransformedInputColumn(String name, IdGenerator idGenerator) {
		_name = name;
		_id = idGenerator.nextId();
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public void setName(String name) {
		_name = name;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public boolean isPhysicalColumn() {
		return false;
	}

	@Override
	public boolean isVirtualColumn() {
		return true;
	}

	@Override
	protected boolean equalsInternal(AbstractInputColumn<?> that) {
		@SuppressWarnings("unchecked")
		TransformedInputColumn<E> that2 = (TransformedInputColumn<E>) that;
		return this.getId().equals(that2.getId());
	}

	@Override
	protected Column getPhysicalColumnInternal() {
		return null;
	}

	@Override
	protected int hashCodeInternal() {
		return _id.hashCode();
	}
}
