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

import java.util.Collection;

import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.runner.ReferenceDataActivationManager;

/**
 * Utility/convenience class for doing simple lifecycle management and/or
 * mimicing the lifecycle of components lifecycle in a job execution.
 * 
 * @author Kasper Sørensen
 */
public final class LifeCycleHelper {

	private final InjectionManager _injectionManager;
	private final ReferenceDataActivationManager _referenceDataActivationManager;

	public LifeCycleHelper(InjectionManager injectionManager, ReferenceDataActivationManager referenceDataActivationManager) {
		_injectionManager = injectionManager;
		_referenceDataActivationManager = referenceDataActivationManager;
	}

	public void assignConfiguredProperties(ComponentDescriptor<?> descriptor, Object component,
			BeanConfiguration beanConfiguration) {
		AssignConfiguredCallback callback = new AssignConfiguredCallback(beanConfiguration, _referenceDataActivationManager);
		callback.onEvent(component, descriptor);
	}

	public void assignProvidedProperties(ComponentDescriptor<?> descriptor, Object component) {
		AssignProvidedCallback callback = new AssignProvidedCallback(_injectionManager);
		callback.onEvent(component, descriptor);
	}

	public void validate(ComponentDescriptor<?> descriptor, Object component) {
		InitializeCallback callback = new InitializeCallback(true, false);
		callback.onEvent(component, descriptor);
	}

	public void initialize(ComponentDescriptor<?> descriptor, Object component) {
		InitializeCallback callback = new InitializeCallback(true, true);
		callback.onEvent(component, descriptor);
	}

	public void close(ComponentDescriptor<?> descriptor, Object component) {
		CloseCallback callback = new CloseCallback();
		callback.onEvent(component, descriptor);
	}

	/**
	 * Closes all reference data used in this life cycle helper
	 */
	public void closeReferenceData() {
		if (_referenceDataActivationManager == null) {
			return;
		}
		final Collection<Object> referenceData = _referenceDataActivationManager.getAllReferenceData();
		for (Object object : referenceData) {
			ComponentDescriptor<? extends Object> descriptor = Descriptors.ofComponent(object.getClass());
			close(descriptor, object);
		}
	}

	/**
	 * Initializes all reference data used in this life cycle helper
	 */
	public void initializeReferenceData() {
		if (_referenceDataActivationManager == null) {
			return;
		}
		final Collection<Object> referenceDataCollection = _referenceDataActivationManager.getAllReferenceData();
		for (Object referenceData : referenceDataCollection) {
			ComponentDescriptor<? extends Object> descriptor = Descriptors.ofComponent(referenceData.getClass());

			assignProvidedProperties(descriptor, referenceData);
			initialize(descriptor, referenceData);
		}
	}
}
