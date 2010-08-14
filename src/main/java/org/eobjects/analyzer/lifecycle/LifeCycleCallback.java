package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.descriptors.BeanDescriptor;

public interface LifeCycleCallback {

	public void onEvent(LifeCycleState state, Object bean,
			BeanDescriptor<?> descriptor);
}
