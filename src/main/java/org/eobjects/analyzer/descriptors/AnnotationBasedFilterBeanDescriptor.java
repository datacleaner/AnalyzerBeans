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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class AnnotationBasedFilterBeanDescriptor<F extends Filter<C>, C extends Enum<C>> extends
		AbstractBeanDescriptor<F> implements FilterBeanDescriptor<F, C> {

	private final String _displayName;

	public static <F extends Filter<C>, C extends Enum<C>> FilterBeanDescriptor<F, C> create(Class<F> filterClass) {
		return new AnnotationBasedFilterBeanDescriptor<F, C>(filterClass);
	}

	/**
	 * Alternative factory method used when sufficient type-information about
	 * the class is not available.
	 * 
	 * This method is basically a hack to make the compiler happy, see Ticket
	 * #417.
	 * 
	 * @see http://eobjects.org/trac/ticket/417
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static FilterBeanDescriptor<?, ?> createUnbound(Class<?> clazz) {
		return new AnnotationBasedFilterBeanDescriptor(clazz);
	}

	private AnnotationBasedFilterBeanDescriptor(Class<F> filterClass) throws DescriptorException {
		super(filterClass, true);

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
	public Class<C> getCategoryEnum() {
		Type[] interfaces = getBeanClass().getGenericInterfaces();
		for (Type type : interfaces) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				if (pType.getRawType() == Filter.class) {
					Class<?> typeParameter = ReflectionUtils.getTypeParameter(pType, 0);
					return (Class<C>) typeParameter;
				}
			}
		}
		throw new IllegalStateException("Could not determine Filter's category enum type");
	}

	@Override
	public EnumSet<C> getCategories() {
		Class<C> categoryEnum = getCategoryEnum();
		return EnumSet.allOf(categoryEnum);
	}

	@Override
	public Set<String> getCategoryNames() {
		EnumSet<C> enumSet = getCategories();
		Set<String> result = new HashSet<String>();
		for (Enum<C> category : enumSet) {
			result.add(category.name());
		}
		return result;
	}

	@Override
	public Enum<C> getCategoryByName(String categoryName) {
		EnumSet<C> categories = getCategories();
		for (Enum<C> c : categories) {
			if (c.name().equals(categoryName)) {
				return c;
			}
		}
		return null;
	}
}
