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

import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.AssignProvidedCallback;
import org.eobjects.analyzer.lifecycle.BeanInstance;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AssignCallbacksAndInitializeTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final BeanInstance<?> _beanInstance;
	private final InjectionManager _injectionManager;

	// represents the default lifecycle callbacks ...
	private final AssignConfiguredCallback _assignConfiguredCallback;
	private final InitializeCallback _initializeCallback;
	private final CloseCallback _closeCallback;

	public AssignCallbacksAndInitializeTask(BeanInstance<?> beanInstance, InjectionManager injectionManager,
			AssignConfiguredCallback assignConfiguredCallback, InitializeCallback initializeCallback,
			CloseCallback closeCallback) {
		_beanInstance = beanInstance;
		_injectionManager = injectionManager;
		_assignConfiguredCallback = assignConfiguredCallback;
		_initializeCallback = initializeCallback;
		_closeCallback = closeCallback;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");

		if (_assignConfiguredCallback != null) {
			_beanInstance.getAssignConfiguredCallbacks().add(_assignConfiguredCallback);
		}

		AssignProvidedCallback assignProvidedCallback = new AssignProvidedCallback(_injectionManager);
		_beanInstance.getAssignProvidedCallbacks().add(assignProvidedCallback);

		if (_initializeCallback != null) {
			_beanInstance.getInitializeCallbacks().add(_initializeCallback);
		}

		if (_closeCallback != null) {
			_beanInstance.getCloseCallbacks().add(_closeCallback);
		}

		logger.debug("calling assignConfigured() on bean: {}", _beanInstance);
		_beanInstance.assignConfigured();

		logger.debug("calling assignProvided() on bean: {}", _beanInstance);
		_beanInstance.assignProvided();

		logger.debug("calling initialize() on bean: {}", _beanInstance);
		_beanInstance.initialize();
	}

	@Override
	public String toString() {
		return "AssignCallbacksAndInitializeTasks[beanInstance=" + _beanInstance + "]";
	}
}
