package org.eobjects.analyzer.result;

import dk.eobjects.metamodel.schema.Table;

public class TableDifference<E> implements StructuralDifference<Table, E> {

	private static final long serialVersionUID = 1L;

	private Table table1;
	private Table table2;
	private String valueName;
	private E value1;
	private E value2;

	public TableDifference(Table table1, Table table2, String valueName,
			E value1, E value2) {
		this.table1 = table1;
		this.table2 = table2;
		this.valueName = valueName;
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public Table getStructure1() {
		return table1;
	}

	@Override
	public Table getStructure2() {
		return table2;
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
		return "Tables '" + getStructure1().getName() + "' and '"
				+ getStructure2().getName() + "' differ on '" + getValueName()
				+ "': [" + getValue1() + "] vs. [" + getValue2() + "]";
	}
}
