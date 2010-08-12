package org.eobjects.analyzer.result;

import java.io.Serializable;

/**
 * 
 * @author Kasper Sørensen
 * 
 * @param <S>
 *            the structural type, ie. Schema, Table, Column, Relationship etc.
 * @param <V>
 *            the value type, ie. the thing that actually differs.
 */
public interface StructuralDifference<S, V> extends Serializable {

	public S getStructure1();

	public S getStructure2();

	public String getValueName();

	public V getValue1();

	public V getValue2();
}
