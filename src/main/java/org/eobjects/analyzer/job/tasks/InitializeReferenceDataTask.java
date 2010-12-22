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

import org.eobjects.analyzer.job.runner.ReferenceDataActivationManager;

/**
 * Task that invokes initializing methods for reference data where this is
 * nescesary.
 * 
 * @author Kasper Sørensen
 */
public class InitializeReferenceDataTask implements Task {

	private final ReferenceDataActivationManager _referenceDataActivationManager;

	public InitializeReferenceDataTask(ReferenceDataActivationManager referenceDataActivationManager) {
		_referenceDataActivationManager = referenceDataActivationManager;
	}

	@Override
	public void execute() throws Exception {
		// TODO: initialize reference data collected by manager
	}

}
