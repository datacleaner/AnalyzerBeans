package org.eobjects.analyzer.reference;

import java.io.Serializable;
import java.util.Collection;

public class SimpleDictionary implements Dictionary, Serializable {

	private static final long serialVersionUID = 1L;

	private String _name;
	private ReferenceValues<String> _values;

	public SimpleDictionary(String name, String... values) {
		_name = name;
		_values = new SimpleReferenceValues<String>(values);
	}

	public SimpleDictionary(String name, Collection<String> values) {
		_name = name;
		_values = new SimpleReferenceValues<String>(values.toArray(new String[values.size()]));
	}

	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public ReferenceValues<String> getValues() {
		return _values;
	}

	@Override
	public boolean containsValue(String value) {
		return _values.containsValue(value);
	}

}
