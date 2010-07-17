package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBeanInstance {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AbstractBeanDescriptor descriptor;
	private Object bean;
	private List<LifeCycleCallback> assignConfiguredCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> assignProvidedCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> initializeCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> closeCallbacks = new LinkedList<LifeCycleCallback>();
	
	public AbstractBeanInstance(AbstractBeanDescriptor descriptor) {
		if (descriptor == null) {
			throw new IllegalArgumentException("Descriptor cannot be null");
		}
		try {
			this.bean = descriptor.getBeanClass().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not instantiate analyzer bean type: "
							+ descriptor.getBeanClass(), e);
		}
		this.descriptor = descriptor;
	}
	
	public Object getBean() {
		return bean;
	}
	
	public AbstractBeanDescriptor getDescriptor() {
		return descriptor;
	}

	public void assignConfigured() {
		if (logger.isInfoEnabled()) {
			logger.info("assignConfigured (" + bean + ")");
		}
		runCallbacks(assignConfiguredCallbacks,
				LifeCycleState.ASSIGN_CONFIGURED);
	}

	public List<LifeCycleCallback> getAssignConfiguredCallbacks() {
		return assignConfiguredCallbacks;
	}

	public void assignProvided() {
		if (logger.isInfoEnabled()) {
			logger.info("assignProvided (" + bean + ")");
		}
		runCallbacks(assignProvidedCallbacks, LifeCycleState.ASSIGN_PROVIDED);
	}

	public List<LifeCycleCallback> getAssignProvidedCallbacks() {
		return assignProvidedCallbacks;
	}

	public void initialize() {
		if (logger.isInfoEnabled()) {
			logger.info("initialize (" + bean + ")");
		}
		runCallbacks(initializeCallbacks, LifeCycleState.INITIALIZE);
	}

	public List<LifeCycleCallback> getInitializeCallbacks() {
		return initializeCallbacks;
	}

	public void close() {
		if (logger.isInfoEnabled()) {
			logger.info("close (" + bean + ")");
		}
		runCallbacks(closeCallbacks, LifeCycleState.CLOSE);
	}

	public List<LifeCycleCallback> getCloseCallbacks() {
		return closeCallbacks;
	}

	private void runCallbacks(List<LifeCycleCallback> callbacks,
			LifeCycleState state) {
		for (LifeCycleCallback lifeCycleCallback : callbacks) {
			lifeCycleCallback.onEvent(state, bean, descriptor);
		}
	}
}
