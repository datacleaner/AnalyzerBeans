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
package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NestedTaskListener implements TaskListener {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleTasksTaskListener.class);

	private final String _name;
	private final AtomicInteger _countDown;
	private final TaskListener _nestedTaskListener;
	private volatile Throwable _error;

	public NestedTaskListener(String name, int tasksToWaitFor, TaskListener nestedTaskListener) {
		_name = name;
		_nestedTaskListener = nestedTaskListener;

		if (tasksToWaitFor == 0) {
			// immediate completion
			_countDown = new AtomicInteger(1);
			onComplete(null);
		} else {
			_countDown = new AtomicInteger(tasksToWaitFor);
		}
	}

	@Override
	public void onBegin(Task task) {
	}

	@Override
	public void onComplete(Task task) {
		int count = _countDown.decrementAndGet();
		logger.debug("onComplete(), count = {}", count);
		invokeNested(count, task);
	}

	@Override
	public void onError(Task task, Throwable throwable) {
		_error = throwable;
		int count = _countDown.decrementAndGet();
		logger.debug("onComplete(), count = {}", count);
		invokeNested(count, task);
	}

	private void invokeNested(final int count, Task task) {
		if (count == 0) {
			if (_error == null) {
				logger.info("Calling onComplete(...) on nested TaskListener ({})", _name);
				_nestedTaskListener.onComplete(task);
			} else {
				logger.info("Calling onError(...) on nested TaskListener ({})", _name);
				_nestedTaskListener.onError(task, _error);
			}
		}
	}
}
