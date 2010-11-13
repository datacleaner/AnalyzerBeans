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
package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

/**
 * Represents an instance of an @FilterBean annotated class at runtime. The
 * FilterBeanInstance class is responsible for performing life-cycle actions at
 * an per-instance level. This makes it possible to add callbacks at various
 * stages in the life-cycle of a FilterBean
 */
public class FilterBeanInstance extends AbstractBeanInstance<Filter<?>> {

	public FilterBeanInstance(FilterBeanDescriptor<?, ?> descriptor) {
		super(descriptor);
	}

}
