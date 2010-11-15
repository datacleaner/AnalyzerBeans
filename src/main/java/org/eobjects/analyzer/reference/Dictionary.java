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
package org.eobjects.analyzer.reference;

import java.io.Serializable;

/**
 * A dictionary represents a set of values grouped together with a label.
 * 
 * Examples of meaningful dictionaries:
 * <ul>
 * <li>Lastnames</li>
 * <li>Female given names</li>
 * <li>Product codes</li>
 * </ul>
 * 
 * Often times a dictionary will implement a caching mechanism to prevent having
 * to hold all values of the dictionary in memory.
 * 
 * @author Kasper Sørensen
 */
public interface Dictionary extends Serializable {

	public String getName();

	public boolean containsValue(String value);

	/**
	 * Gets the dictionaries contents as a ReferenceValues object. Use with
	 * caution because this might require the dictionary to do eager
	 * initialization of all values.
	 * 
	 * @return
	 */
	public ReferenceValues<String> getValues();
}
