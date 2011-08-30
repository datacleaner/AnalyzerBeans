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

import java.util.Arrays;
import java.util.Collection;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task listener that will fork into a new set of tasks, once it's predecessors
 * have ben completed.
 * 
 * @author Kasper Sørensen
 */
public final class ForkTaskListener implements TaskListener {

	private static final Logger logger = LoggerFactory.getLogger(ForkTaskListener.class);

	private final TaskRunner _taskRunner;
	private final Collection<TaskRunnable> _tasks;
	private final String _whatAreYouWaitingFor;

	public ForkTaskListener(String whatAreYouWaitingFor, TaskRunner taskRunner, TaskRunnable... tasksToSchedule) {
		this(whatAreYouWaitingFor, taskRunner, Arrays.asList(tasksToSchedule));
	}

	public ForkTaskListener(String whatAreYouWaitingFor, TaskRunner taskRunner, Collection<TaskRunnable> tasksToSchedule) {
		_whatAreYouWaitingFor = whatAreYouWaitingFor;
		_taskRunner = taskRunner;
		_tasks = tasksToSchedule;
	}

	@Override
	public void onComplete(Task task) {
		logger.debug("onComplete({})", _whatAreYouWaitingFor);
		logger.info("Scheduling {} tasks", _tasks.size());
		for (TaskRunnable tr : _tasks) {
			_taskRunner.run(tr);
		}
	}

	public void onBegin(Task task) {
		// do nothing
	};

	@Override
	public void onError(Task task, Throwable throwable) {
		for (TaskRunnable tr : _tasks) {
			TaskListener listener = tr.getListener();
			if (listener == null) {
				logger.warn("TaskListener for {} was null", tr);
			} else {
				listener.onError(task, throwable);
			}
		}
	}
}
