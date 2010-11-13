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
package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.job.AbstractFilterOutcome;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;

public final class LazyFilterOutcome extends AbstractFilterOutcome implements FilterOutcome {

	private FilterJobBuilder<?, ?> _filterJobBuilder;
	private Enum<?> _category;

	public LazyFilterOutcome(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
		_filterJobBuilder = filterJobBuilder;
		_category = category;
	}

	@Override
	public FilterJob getFilterJob() {
		return _filterJobBuilder.toFilterJob();
	}

	@Override
	public Enum<?> getCategory() {
		return _category;
	}
}
