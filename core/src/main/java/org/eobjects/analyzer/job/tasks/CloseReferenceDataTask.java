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
package org.eobjects.analyzer.job.tasks;

import java.util.Collection;

import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.runner.ReferenceDataActivationManager;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.LifeCycleState;

/**
 * Task that calls closing methods for reference data where this is nescesary.
 * 
 * @author Kasper Sørensen
 */
public final class CloseReferenceDataTask implements Task {

	private final ReferenceDataActivationManager _referenceDataActivationManager;

	public CloseReferenceDataTask(ReferenceDataActivationManager referenceDataActivationManager) {
		_referenceDataActivationManager = referenceDataActivationManager;
	}

	@Override
	public void execute() throws Exception {
		Collection<Object> referenceData = _referenceDataActivationManager.getAllReferenceData();
		for (Object object : referenceData) {
			ComponentDescriptor<? extends Object> descriptor = Descriptors.ofComponent(object.getClass());
			new CloseCallback().onEvent(LifeCycleState.CLOSE, object, descriptor);
		}
	}

}
