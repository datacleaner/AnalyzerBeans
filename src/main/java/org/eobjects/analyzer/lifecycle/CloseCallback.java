package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;
import org.eobjects.analyzer.descriptors.CloseDescriptor;

public class CloseCallback implements LifeCycleCallback {

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AbstractBeanDescriptor descriptor) {
		assert state == LifeCycleState.CLOSE;

		List<CloseDescriptor> closeDescriptors = descriptor
				.getCloseDescriptors();
		for (CloseDescriptor closeDescriptor : closeDescriptors) {
			closeDescriptor.close(analyzerBean);
		}
	}

}
