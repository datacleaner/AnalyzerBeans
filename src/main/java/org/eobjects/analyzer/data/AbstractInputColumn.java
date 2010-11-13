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

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends E> getDataType() {
		return (Class<? extends E>) getDataTypeFamily().getJavaType();
	}

	@Override
	public Column getPhysicalColumn() throws IllegalStateException {
		if (!isPhysicalColumn()) {
			throw new IllegalStateException(this + " is not a physical InputColumn");
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
		return getClass().hashCode() + hashCodeInternal();
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
			if (that.getDataTypeFamily() == this.getDataTypeFamily()) {
				if (that.isPhysicalColumn() == this.isPhysicalColumn()) {
					return equalsInternal(that);
				}
			}
		}
		return false;
	}
}
