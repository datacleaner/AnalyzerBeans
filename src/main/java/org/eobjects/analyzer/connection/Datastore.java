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
package org.eobjects.analyzer.connection;

import java.io.Serializable;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.metamodel.util.HasName;

/**
 * Defines a datastore from which data can be queried.
 * 
 * Datastores are kept in the {@link AnalyzerBeansConfiguration}, or rather in
 * the {@link DatastoreCatalog}.
 * 
 * @author Kasper Sørensen
 */
public interface Datastore extends Serializable, HasName {

	/**
	 * Gets the name of the datastore
	 * 
	 * @return a String name
	 */
	@Override
	public String getName();

	/**
	 * Gets an optional description of the datastore.
	 * 
	 * @return a String description, or null if no description is available.
	 */
	public String getDescription();

	/**
	 * Sets the description of the datastore.
	 * 
	 * @param description
	 *            the new description of the datastore.
	 */
	public void setDescription(String description);

	/**
	 * Gets a DataContextProvider, which is to be treated as a "connection" to
	 * the datastore - a handle that allows you to query and explore the
	 * datastore.
	 * 
	 * @return a DataContextProvider for this datastore.
	 */
	public DataContextProvider getDataContextProvider();

	/**
	 * Gets the performance characteristics of this datastore.
	 * 
	 * @return the performance characteristics of this datastore.
	 */
	public PerformanceCharacteristics getPerformanceCharacteristics();
}
