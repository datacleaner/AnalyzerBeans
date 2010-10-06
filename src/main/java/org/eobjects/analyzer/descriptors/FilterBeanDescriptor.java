package org.eobjects.analyzer.descriptors;

import java.util.Set;

import org.eobjects.analyzer.beans.api.Filter;

public interface FilterBeanDescriptor<F extends Filter<C>, C extends Enum<C>> extends BeanDescriptor<F> {

	public Class<C> getCategoryEnum();
	
	public Set<String> getCategoryNames();
}
