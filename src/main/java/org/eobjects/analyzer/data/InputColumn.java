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

/**
 * Represents a column that is used to retrieve values from a row of data. A
 * column can be either physical (based directly on a database column) or
 * virtual if the values yielded by this column are have been transformed (eg.
 * tokenized, sampled etc.)
 * 
 * @see DataTypeFamily
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 *            the data type of the column. See <code>DataTypeFamily</code> for
 *            available values.
 */
public interface InputColumn<E> extends Comparable<InputColumn<E>> {

	/**
	 * @return the name of this column
	 */
	public String getName();

	/**
	 * @return true if this InputColumn is based on a physical column that can
	 *         be natively queried.
	 */
	public boolean isPhysicalColumn();

	/**
	 * @return true if this InputColumn is virtual (ie. the opposite of
	 *         physical).
	 */
	public boolean isVirtualColumn();

	/**
	 * @return the underlying physical column.
	 * @throws IllegalStateException
	 *             if isPhysicalColumn() is false
	 */
	public Column getPhysicalColumn() throws IllegalStateException;

	/**
	 * The Data type stored in this column represented as a Java type. Notice
	 * that for most purposes you should ud getDataTypeFamily as it is limited
	 * to the actual supported families of data types (eg. File is not a
	 * supported data type, but Integer, Float, String etc. are).
	 * 
	 * @return the data type of this column.
	 */
	public Class<? extends E> getDataType();

	/**
	 * @return the family of datatypes that matches column's data type. This
	 *         value should be corresponding to the type-parameter <E>.
	 */
	public DataTypeFamily getDataTypeFamily();
}
