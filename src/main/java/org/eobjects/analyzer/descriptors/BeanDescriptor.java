package org.eobjects.analyzer.descriptors;

import java.util.Set;

import org.eobjects.analyzer.data.DataTypeFamily;

public interface BeanDescriptor extends Comparable<BeanDescriptor> {

	public String getDisplayName();

	public Class<?> getBeanClass();
	
	public Set<ConfiguredPropertyDescriptor> getConfiguredProperties();
	
	public ConfiguredPropertyDescriptor getConfiguredProperty(String name);
	
	public ConfiguredPropertyDescriptor getConfiguredPropertyForInput();
	
	public Set<ProvidedPropertyDescriptor> getProvidedProperties();
	
	public Set<InitializeMethodDescriptor> getInitializeMethods();
	
	public Set<CloseMethodDescriptor> getCloseMethods();
	
	public DataTypeFamily getInputDataTypeFamily();
}
