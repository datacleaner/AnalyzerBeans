package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

/**
 * Represents an instance of an @AnalyzerBean annotated class at runtime. The
 * AnalyzerBeanInstance class is responsible for performing life-cycle actions
 * at an per-instance level. This makes it possible to add callbacks at various
 * stages in the life-cycle of an AnalyzerBean
 */
public class AnalyzerBeanInstance implements Runnable {

	private Object analyzerBean;
	private AnalyzerBeanDescriptor descriptor;
	private List<LifeCycleCallback> assignConfiguredCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> assignProvidedCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> initializeCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> runCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> returnResultsCallbacks = new LinkedList<LifeCycleCallback>();
	private List<LifeCycleCallback> closeCallbacks = new LinkedList<LifeCycleCallback>();

	public AnalyzerBeanInstance(Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		if (analyzerBean == null || descriptor == null) {
			throw new NullPointerException();
		}
		this.analyzerBean = analyzerBean;
		this.descriptor = descriptor;
	}
	
	public void assignConfigured() {
		runCallbacks(assignConfiguredCallbacks,
				LifeCycleState.ASSIGN_CONFIGURED);
	}
	
	public List<LifeCycleCallback> getAssignConfiguredCallbacks() {
		return assignConfiguredCallbacks;
	}

	public void assignProvided() {
		runCallbacks(assignProvidedCallbacks, LifeCycleState.ASSIGN_PROVIDED);
	}
	
	public List<LifeCycleCallback> getAssignProvidedCallbacks() {
		return assignProvidedCallbacks;
	}
	
	public void initialize() {
		runCallbacks(initializeCallbacks, LifeCycleState.INITIALIZE);
	}
	
	public List<LifeCycleCallback> getInitializeCallbacks() {
		return initializeCallbacks;
	}

	public void run() {
		runCallbacks(runCallbacks, LifeCycleState.RUN);
	}
	
	public List<LifeCycleCallback> getRunCallbacks() {
		return runCallbacks;
	}

	public void returnResults() {
		runCallbacks(returnResultsCallbacks, LifeCycleState.RETURN_RESULTS);
	}
	
	public List<LifeCycleCallback> getReturnResultsCallbacks() {
		return returnResultsCallbacks;
	}

	public void close() {
		runCallbacks(closeCallbacks, LifeCycleState.CLOSE);
	}
	
	public List<LifeCycleCallback> getCloseCallbacks() {
		return closeCallbacks;
	}

	private void runCallbacks(List<LifeCycleCallback> callbacks,
			LifeCycleState state) {
		for (LifeCycleCallback lifeCycleCallback : callbacks) {
			lifeCycleCallback.onEvent(state, analyzerBean, descriptor);
		}
	}
}
