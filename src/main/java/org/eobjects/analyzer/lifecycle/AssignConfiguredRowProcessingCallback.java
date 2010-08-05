package org.eobjects.analyzer.lifecycle;

import java.util.Arrays;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.BeanConfiguration;

public class AssignConfiguredRowProcessingCallback extends
		AssignConfiguredCallback {

	private final InputColumn<?>[] _localInputColumns;

	public AssignConfiguredRowProcessingCallback(
			BeanConfiguration beanConfiguration,
			InputColumn<?>[] localInputColumns) {
		super(beanConfiguration);
		_localInputColumns = localInputColumns;
	}

	@Override
	protected Object getValue(ConfiguredPropertyDescriptor propertyDescriptor) {
		if (propertyDescriptor.isInputColumn()) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"ConfiguredDescriptor is InputColumn type. Returning: {}",
						Arrays.toString(_localInputColumns));
			}
			return _localInputColumns;
		}
		return super.getValue(propertyDescriptor);
	}
}
