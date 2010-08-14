package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.eobjects.analyzer.data.DataTypeFamily;

/**
 * 
 * @author Kasper Sørensen
 * 
 * @param <B>
 *            the Bean type
 */
public interface BeanDescriptor<B> extends Comparable<BeanDescriptor<?>> {

	public String getDisplayName();

	public Set<Annotation> getAnnotations();

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass);

	public Class<B> getBeanClass();

	public Set<ConfiguredPropertyDescriptor> getConfiguredProperties();

	public ConfiguredPropertyDescriptor getConfiguredProperty(String name);

	public ConfiguredPropertyDescriptor getConfiguredPropertyForInput();

	public Set<ProvidedPropertyDescriptor> getProvidedProperties();

	public Set<InitializeMethodDescriptor> getInitializeMethods();

	public Set<CloseMethodDescriptor> getCloseMethods();

	public DataTypeFamily getInputDataTypeFamily();
}
