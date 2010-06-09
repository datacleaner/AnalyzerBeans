package org.eobjects.analyzer.reference;

import java.io.Serializable;

public class SimpleDictionary implements Dictionary, Serializable {

	private static final long serialVersionUID = 1L;

	private String _name;
	private ReferenceValues<String> _values;

	public SimpleDictionary(String name, String... values) {
		_name = name;
		_values = new SimpleReferenceValues<String>(values);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public ReferenceValues<String> getValues() {
		return _values;
	}

}
