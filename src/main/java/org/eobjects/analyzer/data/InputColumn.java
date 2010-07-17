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
 *            the data type family of the column. See
 *            <code>DataTypeFamily</code> for available values.
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
	 * @return the family of datatypes that matches column's data type. This
	 *         value should be corresponding to the type-parameter <E>.
	 */
	public DataTypeFamily getDataTypeFamily();
}
