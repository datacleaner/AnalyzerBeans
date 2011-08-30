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

import java.io.Closeable;

import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CollectResultsAndCloseAnalyzerBeanTask extends CloseBeanTask {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AnalyzerBeanInstance _analyzerBeanInstance;
	private Closeable[] _closeables;

	public CollectResultsAndCloseAnalyzerBeanTask(AnalyzerBeanInstance analyzerBeanInstance, Closeable... closeables) {
		super(analyzerBeanInstance);
		_analyzerBeanInstance = analyzerBeanInstance;
		_closeables = closeables;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");
		_analyzerBeanInstance.returnResults();

		super.execute();

		for (int i = 0; i < _closeables.length; i++) {
			_closeables[i].close();
		}
	}

}
