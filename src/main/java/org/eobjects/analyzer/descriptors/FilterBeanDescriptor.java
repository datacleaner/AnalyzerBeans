package org.eobjects.analyzer.descriptors;

import java.util.EnumSet;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Filter;

public interface FilterBeanDescriptor<F extends Filter<C>, C extends Enum<C>> extends BeanDescriptor<F> {

	public Class<C> getCategoryEnum();

	public EnumSet<C> getCategories();

	public Set<String> getCategoryNames();

	public Enum<C> getCategoryByName(String category);
}