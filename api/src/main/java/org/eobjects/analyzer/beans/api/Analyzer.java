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

import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * Super-interface for analyzers. Analyzers should implement one of the two
 * sub-interfaces, ExploringAnalyzer or RowProcessingAnalyzer.
 * 
 * @see ExploringAnalyzer
 * @see RowProcessingAnalyzer
 * 
 * @author Kasper Sørensen
 * 
 * @param <R>
 *            the result type of this analyzer.
 */
public interface Analyzer<R extends AnalyzerResult> {

	/**
	 * Gets the result of the analysis that the analyzer has conducted.
	 * 
	 * @return an analyzer result object.
	 */
	public R getResult();
}
