package org.eobjects.analyzer.beans.valuedist;

import java.io.Serializable;

public class ValueCount implements Serializable {

	private static final long serialVersionUID = 1L;

	private String _value;
	private int _count;

	public ValueCount(String value, int count) {
		_value = value;
		_count = count;
	}

	public String getValue() {
		return _value;
	}

	public int getCount() {
		return _count;
	}

	@Override
	public String toString() {
		return "[" + _value + "->" + _count + "]";
	}
}
