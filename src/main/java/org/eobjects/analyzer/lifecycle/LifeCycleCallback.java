package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;

public interface LifeCycleCallback {

	public void onEvent(LifeCycleState state, Object bean,
			AbstractBeanDescriptor descriptor);
}
