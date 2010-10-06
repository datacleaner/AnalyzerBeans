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
	public Set<String> getCategoryNames() {
		Class<C> categoryEnum = getCategoryEnum();
		EnumSet<C> enumSet = EnumSet.allOf(categoryEnum);
		Set<String> result = new HashSet<String>();
		for (Enum<C> category : enumSet) {
			result.add(category.name());
		}
		return result;
	}
}
