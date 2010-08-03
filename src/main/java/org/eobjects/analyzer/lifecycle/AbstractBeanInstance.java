package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author kasper
 *
 * @param <E> the bean type, typically Analyzer or Transformer
 */
public abstract class AbstractBeanInstance<E> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private BeanDescriptor descriptor;
	private E bean;
	private List<LifeCycleCallback> assignConfiguredCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> assignProvidedCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> initializeCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> closeCallbacks = new LinkedList<LifeCycleCallback>();

	@SuppressWarnings("unchecked")
	public AbstractBeanInstance(BeanDescriptor descriptor) {
		if (descriptor == null) {
			throw new IllegalArgumentException("Descriptor cannot be null");
		}
		try {
			this.bean = (E) descriptor.getBeanClass().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not instantiate analyzer bean type: "
							+ descriptor.getBeanClass(), e);
		}
		this.descriptor = descriptor;
	}

	public E getBean() {
		return bean;
	}

	public BeanDescriptor getDescriptor() {
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
		logger.debug("running {} callbacks: {}", callbacks.size(), callbacks);
		for (LifeCycleCallback lifeCycleCallback : callbacks) {
			lifeCycleCallback.onEvent(state, bean, descriptor);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[descriptor=" + descriptor + "]";
	}
}
