/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.util.ReflectionUtils;
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
	private final List<AssignConfiguredCallback> assignConfiguredCallbacks = new LinkedList<AssignConfiguredCallback>();
	private final List<AssignProvidedCallback> assignProvidedCallbacks = new LinkedList<AssignProvidedCallback>();
	private final List<InitializeCallback> initializeCallbacks = new LinkedList<InitializeCallback>();
	private final List<CloseCallback> closeCallbacks = new LinkedList<CloseCallback>();

	@SuppressWarnings("unchecked")
	public AbstractBeanInstance(BeanDescriptor<?> descriptor) {
		if (descriptor == null) {
			throw new IllegalArgumentException("Descriptor cannot be null");
		}
		_bean = (E) ReflectionUtils.newInstance(descriptor.getComponentClass());
		_descriptor = descriptor;
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

	public List<AssignConfiguredCallback> getAssignConfiguredCallbacks() {
		return assignConfiguredCallbacks;
	}

	public void assignProvided() {
		if (logger.isInfoEnabled()) {
			logger.info("assignProvided (" + _bean + ")");
		}
		runCallbacks(assignProvidedCallbacks, LifeCycleState.ASSIGN_PROVIDED);
	}

	public List<AssignProvidedCallback> getAssignProvidedCallbacks() {
		return assignProvidedCallbacks;
	}

	public void initialize() {
		if (logger.isInfoEnabled()) {
			logger.info("initialize (" + _bean + ")");
		}
		runCallbacks(initializeCallbacks, LifeCycleState.INITIALIZE);
	}

	public List<InitializeCallback> getInitializeCallbacks() {
		return initializeCallbacks;
	}

	public void close() {
		if (logger.isInfoEnabled()) {
			logger.info("close (" + _bean + ")");
		}
		runCallbacks(closeCallbacks, LifeCycleState.CLOSE);
	}

	public List<CloseCallback> getCloseCallbacks() {
		return closeCallbacks;
	}

	private void runCallbacks(List<? extends LifeCycleCallback<Object, ? super BeanDescriptor<?>>> callbacks,
			LifeCycleState state) {
		logger.debug("running {} callbacks: {}", callbacks.size(), callbacks);
		for (LifeCycleCallback<Object, ? super BeanDescriptor<?>> lifeCycleCallback : callbacks) {
			lifeCycleCallback.onEvent(state, _bean, _descriptor);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[descriptor=" + _descriptor + ",bean=" + _bean + "]";
	}
}
