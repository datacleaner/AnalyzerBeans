package org.eobjects.analyzer.lifecycle;

import java.util.Set;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.CloseMethodDescriptor;

public class CloseCallback implements LifeCycleCallback {

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			BeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.CLOSE;

		Set<CloseMethodDescriptor> closeMethods = descriptor
				.getCloseMethods();
		for (CloseMethodDescriptor closeDescriptor : closeMethods) {
			closeDescriptor.close(analyzerBean);
		}
	}

}
