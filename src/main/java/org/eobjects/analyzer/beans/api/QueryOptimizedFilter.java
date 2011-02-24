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
package org.eobjects.analyzer.beans.api;

import org.eobjects.metamodel.query.Query;

/**
 * A filter which can, under certain circumstances, be optimized by using a
 * "push down" technique, where the filter's functionality is expressed in the
 * query that feeds the job that the filter is entered into.
 * 
 * Query optimized filters should implement BOTH the regular categorize(...)
 * method and the optimization-methods in this interface. There is no guarantee
 * that the filter will be optimizing the query, but in cases where a filter is
 * among the first steps in a job, and all succeeding steps depend on a single
 * outcome of the particular filter, it will be allowed to optimize the query.
 * 
 * @author Kasper Sørensen
 * 
 * @param <C>
 *            the filter category enum
 */
public interface QueryOptimizedFilter<C extends Enum<C>> extends Filter<C> {

	public boolean isOptimizable(C category);

	public Query optimizeQuery(Query q, C category);
}
