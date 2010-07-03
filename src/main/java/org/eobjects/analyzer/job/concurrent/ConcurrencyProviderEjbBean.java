package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class ConcurrencyProviderEjbBean implements ConcurrencyProviderEjb {

	private static final Logger logger = LoggerFactory
			.getLogger(ConcurrencyProviderEjbBean.class);

	// shared queue of callables
	private static final BlockingQueue<Callable<?>> callableQueue = new LinkedBlockingQueue<Callable<?>>();

	@Resource
	TimerService timerService;

	@Override
	public <T> Future<T> exec(Callable<T> callable) {
		callableQueue.add(callable);
		timerService.createTimer(0, null);
		
		// TODO: Is there a way to return a Future for this?
		return null;
	}

	@Override
	public void exec(Runnable runnable) {
		callableQueue.add(new RunnableCallable(runnable));
		timerService.createTimer(0, null);
	}

	@Timeout
	public void executeTimer(Timer timer) {
		logger.info("executeTimer(...)");
		Callable<?> callable = callableQueue.poll();
		try {
			callable.call();
		} catch (Exception e) {
			logger.error("An uncaught exception was thrown by callable", e);
		}
	}
}
