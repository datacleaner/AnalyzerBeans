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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.util.ReflectionUtils;

final class AnnotationBasedFilterBeanDescriptor<F extends Filter<C>, C extends Enum<C>> extends
		AbstractBeanDescriptor<F> implements FilterBeanDescriptor<F, C> {

	private final String _displayName;

	protected AnnotationBasedFilterBeanDescriptor(Class<F> filterClass) throws DescriptorException {
		super(filterClass, false);

		if (!ReflectionUtils.is(filterClass, Filter.class)) {
			throw new DescriptorException(filterClass + " does not implement " + Filter.class.getName());
		}

		FilterBean filterAnnotation = filterClass.getAnnotation(FilterBean.class);
		if (filterAnnotation == null) {
			throw new DescriptorException(filterClass + " doesn't implement the FilterBean annotation");
		}

		String displayName = filterAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(filterClass.getSimpleName(), false);
		}
		_displayName = displayName;

		visitClass();
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<C> getOutcomeCategoryEnum() {
		Class<?> typeParameter = ReflectionUtils.getTypeParameter(getComponentClass(), Filter.class, 0);
		if (typeParameter == null) {
			throw new IllegalStateException("Could not determine Filter's category enum type");
		}
		return (Class<C>) typeParameter;
	}

	@Override
	public EnumSet<C> getOutcomeCategories() {
		Class<C> categoryEnum = getOutcomeCategoryEnum();
		return EnumSet.allOf(categoryEnum);
	}

	@Override
	public Set<String> getOutcomeCategoryNames() {
		EnumSet<C> enumSet = getOutcomeCategories();
		Set<String> result = new HashSet<String>();
		for (Enum<C> category : enumSet) {
			result.add(category.name());
		}
		return result;
	}

	@Override
	public Enum<C> getOutcomeCategoryByName(String categoryName) {
		EnumSet<C> categories = getOutcomeCategories();
		for (Enum<C> c : categories) {
			if (c.name().equals(categoryName)) {
				return c;
			}
		}
		return null;
	}
}
