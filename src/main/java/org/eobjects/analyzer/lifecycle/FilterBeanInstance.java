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
