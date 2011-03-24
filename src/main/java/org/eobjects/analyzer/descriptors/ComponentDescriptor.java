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

import java.util.Set;

/**
 * Defines an interface for descriptors of components that support
 * initialization, closing and configuration properties.
 * 
 * @author Kasper Sørensen
 */
public interface ComponentDescriptor<B> extends Comparable<ComponentDescriptor<?>> {

	/**
	 * Gets the component's class
	 * 
	 * @return the component's class
	 */
	public Class<B> getComponentClass();

	/**
	 * Gets all configuration properties of the component
	 * 
	 * @return a set of all properties
	 */
	public Set<ConfiguredPropertyDescriptor> getConfiguredProperties();

	/**
	 * Gets all configuration properties of a particular type (including
	 * subtypes)
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
	 * @param name
	 *            the name of the property
	 * @return a configured property, or null if no such property exists
	 */
	public ConfiguredPropertyDescriptor getConfiguredProperty(String name);

	/**
	 * Gets the initialize methods of the component
	 * 
	 * @return a set of initialize method descriptors
	 */
	public Set<InitializeMethodDescriptor> getInitializeMethods();

	/**
	 * Gets the close methods of the component
	 * 
	 * @return a set of close method descriptors
	 */
	public Set<CloseMethodDescriptor> getCloseMethods();
}
