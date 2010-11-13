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
package org.eobjects.analyzer.data;

/**
 * Represents a column that is mutable (editable by the user).
 * 
 * Mutable columns have editable names but unique id's to identify them (whereas
 * the names identify the immutable columns).
 * 
 * @author Kasper Sørensen
 */
public interface MutableInputColumn<E> extends InputColumn<E> {

	/**
	 * Sets the name of the column
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @return an id that is unique within the AnalysisJob that is being built
	 *         or executed.
	 */
	public String getId();
}
