/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Defines an abstract descriptor for beans (Analyzers, Transformers, Filters)
 * that support configured properties, provided properties, initialize methods
 * and close methods.
 * 
 * @author Kasper Sørensen
 * 
 * @param <B>
 *            the Bean type
 */
public interface BeanDescriptor<B> extends ComponentDescriptor<B> {

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

	public Class<B> getComponentClass();

	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput();

	public Set<ProvidedPropertyDescriptor> getProvidedProperties();
}
