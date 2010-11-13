/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.data;

import java.io.Serializable;

import org.eobjects.analyzer.job.IdGenerator;

import dk.eobjects.metamodel.schema.Column;

public class TransformedInputColumn<E> extends AbstractInputColumn<E> implements MutableInputColumn<E>, Serializable {

	private static final long serialVersionUID = 1L;

	private final String _id;
	private final DataTypeFamily _type;
	private String _name;

	public TransformedInputColumn(String name, DataTypeFamily type, IdGenerator idGenerator) {
		_name = name;
		if (type == null) {
			_type = DataTypeFamily.UNDEFINED;
		} else {
			_type = type;
		}
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

	@Override
	public DataTypeFamily getDataTypeFamily() {
		return _type;
	}

	@Override
	public String toString() {
		return "TransformedInputColumn[id=" + _id + ",name=" + _name + ",type=" + _type + "]";
	}
}
