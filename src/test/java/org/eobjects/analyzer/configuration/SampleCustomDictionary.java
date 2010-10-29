package org.eobjects.analyzer.configuration;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceValues;
import org.eobjects.analyzer.reference.SimpleStringReferenceValues;
import org.junit.Ignore;

@Ignore
public class SampleCustomDictionary implements Dictionary {

	private static final long serialVersionUID = 1L;

	@Configured
	String name;

	@Configured
	int values;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean containsValue(String value) {
		return getValues().containsValue(value);
	}

	@Override
	public ReferenceValues<String> getValues() {
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < this.values; i++) {
			values.add("value" + i);
		}
		SimpleStringReferenceValues refValues = new SimpleStringReferenceValues(values, true);
		return refValues;
	}

}
