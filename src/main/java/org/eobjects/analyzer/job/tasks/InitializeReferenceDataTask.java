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

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.SimpleComponentDescriptor;
import org.eobjects.analyzer.job.runner.ReferenceDataActivationManager;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.LifeCycleState;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;

/**
 * Task that invokes initializing methods for reference data where this is
 * nescesary.
 * 
 * @author Kasper Sørensen
 */
public class InitializeReferenceDataTask implements Task {

	private final ReferenceDataActivationManager _referenceDataActivationManager;
	private DatastoreCatalog _datastoreCatalog;
	private ReferenceDataCatalog _referenceDataCatalog;

	public InitializeReferenceDataTask(DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
			ReferenceDataActivationManager referenceDataActivationManager) {
		_datastoreCatalog = datastoreCatalog;
		_referenceDataCatalog = referenceDataCatalog;
		_referenceDataActivationManager = referenceDataActivationManager;
	}

	@Override
	public void execute() throws Exception {
		Collection<Object> referenceData = _referenceDataActivationManager.getAllReferenceData();
		for (Object object : referenceData) {
			SimpleComponentDescriptor<? extends Object> descriptor = SimpleComponentDescriptor.create(object.getClass());
			new InitializeCallback(_datastoreCatalog, _referenceDataCatalog).onEvent(LifeCycleState.INITIALIZE, object,
					descriptor);
		}
	}

}
