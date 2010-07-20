package org.eobjects.analyzer.lifecycle;

import java.util.Arrays;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.job.BeanConfiguration;

public class AssignConfiguredRowProcessingCallback extends
		AssignConfiguredCallback {

	private InputColumn<?>[] _localInputColumns;

	public AssignConfiguredRowProcessingCallback(
			BeanConfiguration beanConfiguration,
			InputColumn<?>[] localInputColumns) {
		super(beanConfiguration);
		_localInputColumns = localInputColumns;
	}

	@Override
	protected Object getConfiguredValue(
			ConfiguredDescriptor configuredDescriptor) {
		if (configuredDescriptor.isInputColumn()) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"ConfiguredDescriptor is InputColumn type. Returning: {}",
						Arrays.toString(_localInputColumns));
			}
			return _localInputColumns;
		}
		return super.getConfiguredValue(configuredDescriptor);
	}
}
