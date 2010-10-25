package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Defines an abstract descriptor for beans that support configured properties,
 * provided properties, initialize methods and close methods.
 * 
 * @author Kasper Sørensen
 * 
 * @param <B>
 *            the Bean type
 */
public interface BeanDescriptor<B> extends Comparable<BeanDescriptor<?>> {

	/**
	 * @return a humanly readable display name for this bean
	 */
	public String getDisplayName();

	/**
	 * @return a humanly readable description of the bean
	 */
	public String getDescription();

	public Set<Annotation> getAnnotations();

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass);

	public Class<B> getBeanClass();

	public Set<ConfiguredPropertyDescriptor> getConfiguredProperties();

	public ConfiguredPropertyDescriptor getConfiguredProperty(String name);

	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput();

	public Set<ProvidedPropertyDescriptor> getProvidedProperties();

	public Set<InitializeMethodDescriptor> getInitializeMethods();

	public Set<CloseMethodDescriptor> getCloseMethods();
}
