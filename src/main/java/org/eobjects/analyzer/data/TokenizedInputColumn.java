package org.eobjects.analyzer.data;

import dk.eobjects.metamodel.schema.Column;

public class TokenizedInputColumn extends AbstractInputColumn<String> {

	private String _name;

	public TokenizedInputColumn(String name) {
		_name = name;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	protected Column getPhysicalColumnInternal() {
		return null;
	}

	@Override
	protected int hashCodeInternal() {
		return _name.hashCode();
	}

	@Override
	protected boolean equalsInternal(AbstractInputColumn<?> that) {
		return this.hashCode() == that.hashCode();
	}

}
