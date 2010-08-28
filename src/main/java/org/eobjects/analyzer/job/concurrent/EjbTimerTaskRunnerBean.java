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

	private static final Logger logger = LoggerFactory
			.getLogger(EjbTimerTaskRunnerBean.class);

	// shared queue of callables
	private static final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<Task>();

	@Resource
	TimerService timerService;

	@Override
	public void run(Task task) {
		logger.debug("run({})", task);
		taskQueue.add(task);
		timerService.createTimer(0, null);
	}

	@Timeout
	public void executeTimer(Timer timer) {
		logger.info("executeTimer(...)");
		Task task = taskQueue.poll();
		try {
			task.execute();
		} catch (Exception e) {
			logger.error("An uncaught exception was thrown by callable", e);
		}
	}

	@Override
	public void shutdown() {
		logger.info("shutdown() called, nothing to do");
	}
}
