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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class EjbTimerTaskRunnerBean implements EjbTimerTaskRunner {

	private static final Logger logger = LoggerFactory.getLogger(EjbTimerTaskRunnerBean.class);

	// shared queue of callables
	private static final BlockingQueue<TaskRunnable> taskQueue = new LinkedBlockingQueue<TaskRunnable>();

	@Resource
	TimerService timerService;

	@Override
	public void run(Task task, TaskListener listener) {
		logger.debug("run({},{})", task, listener);
		taskQueue.add(new TaskRunnable(task, listener));
		timerService.createTimer(0, null);
	}

	@Override
	public void run(TaskRunnable taskRunnable) {
		logger.debug("run({})", taskRunnable);
		taskQueue.add(taskRunnable);
		timerService.createTimer(0, null);
	}

	@Timeout
	public void executeTimer(Timer timer) {
		logger.info("executeTimer(...)");
		TaskRunnable task = taskQueue.poll();
		task.run();
	}

	@Override
	public void shutdown() {
		logger.info("shutdown() called, nothing to do");
	}
}
