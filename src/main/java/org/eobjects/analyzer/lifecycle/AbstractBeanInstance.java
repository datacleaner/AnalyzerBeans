package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for use of bean instances at runtime. A bean instance is a
 * wrapper for the actual beans that get instantiated during a job execution.
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 *            the bean type, ie. Filter, Analyzer or Transformer
 */
public abstract class AbstractBeanInstance<E> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final BeanDescriptor<?> _descriptor;
	private final E _bean;
	private final List<LifeCycleCallback> assignConfiguredCallbacks = new LinkedList<LifeCycleCallback>();
	private final List<LifeCycleCallback> assignProvidedCallbacks = new LinkedList<LifeCycleCallback>();
	private final List<LifeCycleCallback> initializeCallbacks = new LinkedList<LifeCycleCallback>();
	private final List<LifeCycleCallback> closeCallbacks = new LinkedList<LifeCycleCallback>();

	@SuppressWarnings("unchecked")
	public AbstractBeanInstance(BeanDescriptor<?> descriptor) {
		if (descriptor == null) {
			throw new IllegalArgumentException("Descriptor cannot be null");
		}
		try {
			this._bean = (E) descriptor.getBeanClass().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not instantiate analyzer bean type: " + descriptor.getBeanClass(), e);
		}
		this._descriptor = descriptor;
	}

	public E getBean() {
		return _bean;
	}

	public BeanDescriptor<?> getDescriptor() {
		return _descriptor;
	}

	public void assignConfigured() {
		if (logger.isInfoEnabled()) {
			logger.info("assignConfigured (" + _bean + ")");
		}
		runCallbacks(assignConfiguredCallbacks, LifeCycleState.ASSIGN_CONFIGURED);
	}

	public List<LifeCycleCallback> getAssignConfiguredCallbacks() {
		return assignConfiguredCallbacks;
	}

	public void assignProvided() {
		if (logger.isInfoEnabled()) {
			logger.info("assignProvided (" + _bean + ")");
		}
		runCallbacks(assignProvidedCallbacks, LifeCycleState.ASSIGN_PROVIDED);
	}

	public List<LifeCycleCallback> getAssignProvidedCallbacks() {
		return assignProvidedCallbacks;
	}

	public void initialize() {
		if (logger.isInfoEnabled()) {
			logger.info("initialize (" + _bean + ")");
		}
		runCallbacks(initializeCallbacks, LifeCycleState.INITIALIZE);
	}

	public List<LifeCycleCallback> getInitializeCallbacks() {
		return initializeCallbacks;
	}

	public void close() {
		if (logger.isInfoEnabled()) {
			logger.info("close (" + _bean + ")");
		}
		runCallbacks(closeCallbacks, LifeCycleState.CLOSE);
	}

	public List<LifeCycleCallback> getCloseCallbacks() {
		return closeCallbacks;
	}

	private void runCallbacks(List<LifeCycleCallback> callbacks, LifeCycleState state) {
		logger.debug("running {} callbacks: {}", callbacks.size(), callbacks);
		for (LifeCycleCallback lifeCycleCallback : callbacks) {
			lifeCycleCallback.onEvent(state, _bean, _descriptor);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[descriptor=" + _descriptor + ",bean=" + _bean + "]";
	}
}
