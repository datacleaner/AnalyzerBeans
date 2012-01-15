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

import java.io.Serializable;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.Validate;

/**
 * Defines an interface for descriptors of components that support
 * initialization, closing and configuration properties.
 * 
 * @author Kasper Sørensen
 */
public interface ComponentDescriptor<B> extends Comparable<ComponentDescriptor<?>>, Serializable {

	/**
	 * Constructs an instance of this component
	 * 
	 * @return a new (uninitialized) instance of the component.
	 */
	public B newInstance();

	/**
	 * Gets the component's class
	 * 
	 * @return the component's class
	 */
	public Class<B> getComponentClass();

	/**
	 * Gets all configuration properties of the component
	 * 
	 * @see Configured
	 * 
	 * @return a set of all properties
	 */
	public Set<ConfiguredPropertyDescriptor> getConfiguredProperties();

	/**
	 * Gets all configuration properties of a particular type (including
	 * subtypes)
	 * 
	 * @see Configured
	 * 
	 * @param type
	 *            the type of property to look for
	 * @param includeArrays
	 *            a boolean indicating whether or not configuration properties
	 *            that are arrays of the provided type should be included
	 * @return a set of properties that match the specified type query
	 */
	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByType(Class<?> type, boolean includeArrays);

	/**
	 * Gets a configured property by name
	 * 
	 * @see Configured
	 * 
	 * @param name
	 *            the name of the property
	 * @return a configured property, or null if no such property exists
	 */
	public ConfiguredPropertyDescriptor getConfiguredProperty(String name);

	/**
	 * Gets the validation methods of the component
	 * 
	 * @see Validate
	 * 
	 * @return a set of validate method descriptors
	 */
	public Set<ValidateMethodDescriptor> getValidateMethods();

	/**
	 * Gets the initialize methods of the component
	 * 
	 * @see Initialize
	 * 
	 * @return a set of initialize method descriptors
	 */
	public Set<InitializeMethodDescriptor> getInitializeMethods();

	/**
	 * Gets the close methods of the component
	 * 
	 * @see Close
	 * 
	 * @return a set of close method descriptors
	 */
	public Set<CloseMethodDescriptor> getCloseMethods();

	/**
	 * Gets the provided properties of the component
	 * 
	 * @see Provided
	 * 
	 * @return a set of provided properties.
	 */
	public Set<ProvidedPropertyDescriptor> getProvidedProperties();

	/**
	 * Gets the provided properties of a particular type in the component
	 * 
	 * @see Provided
	 * 
	 * @param cls
	 *            the type of the provided properties
	 * @return a set of provided properties.
	 */
	public Set<ProvidedPropertyDescriptor> getProvidedPropertiesByType(Class<?> cls);
}
