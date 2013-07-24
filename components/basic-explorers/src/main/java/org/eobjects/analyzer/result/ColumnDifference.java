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
package org.eobjects.analyzer.result;

import org.eobjects.metamodel.schema.Column;

public class ColumnDifference<E> implements StructuralDifference<Column, E> {

	private static final long serialVersionUID = 1L;

	private Column column1;
	private Column column2;
	private String valueName;
	private E value1;
	private E value2;

	public ColumnDifference(Column column1, Column column2, String valueName, E value1, E value2) {
		this.column1 = column1;
		this.column2 = column2;
		this.valueName = valueName;
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public Column getStructure1() {
		return column1;
	}

	@Override
	public Column getStructure2() {
		return column2;
	}

	@Override
	public String getValueName() {
		return valueName;
	}

	@Override
	public E getValue1() {
		return value1;
	}

	@Override
	public E getValue2() {
		return value2;
	}

	@Override
	public String toString() {
		return "Columns '" + getStructure1().getName() + "' and '" + getStructure2().getName() + "' differ on '"
				+ getValueName() + "': [" + getValue1() + "] vs. [" + getValue2() + "]";
	}
}
