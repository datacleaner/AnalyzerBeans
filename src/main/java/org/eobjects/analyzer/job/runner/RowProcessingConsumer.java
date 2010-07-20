package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.BeanJob;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;

interface RowProcessingConsumer {

	public InputColumn<?>[] getRequiredInput();

	public InputRow consume(InputRow row, int distinctCount);

	public AbstractBeanInstance getBeanInstance();
	
	public BeanJob<?> getBeanJob();
}
