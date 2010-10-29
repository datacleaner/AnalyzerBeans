package org.eobjects.analyzer.reference;

import java.io.Serializable;

public interface Dictionary extends Serializable {

	public String getName();
	
	public boolean containsValue(String value);
	
	public ReferenceValues<String> getValues();
}
