package org.eobjects.analyzer.descriptors;

public interface ProvidedPropertyDescriptor extends PropertyDescriptor {

	public boolean isList();

	public boolean isMap();

	public boolean isSet();

	public boolean isDataContext();

	public boolean isSchemaNavigator();

}
