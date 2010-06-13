package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A callable that waits for a collection of futures before it proceeds by
 * scheduling a new collection of callables. This callable can be used to
 * synchronize a lot of threads before continuing with new tasks.
 * 
 * @author Kasper Sørensen
 */
public class SynchronizeCallable implements Callable<Collection<Future<?>>> {

	private static final Logger logger = LoggerFactory
			.getLogger(SynchronizeCallable.class);

	private Collection<Future<?>> futuresToWaitFor;
	private Collection<Callable<?>> callablesToSchedule;
	private ConcurrencyProvider concurrencyProvider;
	private String name;

	public SynchronizeCallable(String name,
			ConcurrencyProvider concurrencyProvider,
			Collection<Future<?>> futuresToWaitFor,
			Collection<Callable<?>> callablesToSchedule) {
		this.name = name;
		this.concurrencyProvider = concurrencyProvider;
		this.futuresToWaitFor = futuresToWaitFor;
		this.callablesToSchedule = callablesToSchedule;
	}

	public SynchronizeCallable(String name,
			ConcurrencyProvider concurrencyProvider, Future<?> futureToWaitFor,
			Collection<Callable<?>> callablesToSchedule) {
		this.name = name;
		this.concurrencyProvider = concurrencyProvider;
		this.futuresToWaitFor = new ArrayList<Future<?>>(1);
		this.futuresToWaitFor.add(futureToWaitFor);
		this.callablesToSchedule = callablesToSchedule;
	}

	public SynchronizeCallable(String name,
			ConcurrencyProvider concurrencyProvider,
			Collection<Future<?>> futuresToWaitFor,
			Callable<?> callableToSchedule) {
		this.name = name;
		this.concurrencyProvider = concurrencyProvider;
		this.futuresToWaitFor = futuresToWaitFor;
		this.callablesToSchedule = new ArrayList<Callable<?>>(1);
		this.callablesToSchedule.add(callableToSchedule);
	}

	public SynchronizeCallable(String name,
			ConcurrencyProvider concurrencyProvider, Future<?> futureToWaitFor,
			Callable<?> callableToSchedule) {
		this.name = name;
		this.concurrencyProvider = concurrencyProvider;
		this.futuresToWaitFor = new ArrayList<Future<?>>(1);
		this.futuresToWaitFor.add(futureToWaitFor);
		this.callablesToSchedule = new ArrayList<Callable<?>>(1);
		this.callablesToSchedule.add(callableToSchedule);
	}

	public SynchronizeCallable(String name,
			ConcurrencyProvider concurrencyProvider,
			Collection<Callable<?>> callablesToSchedule) {
		this.name = name;
		this.concurrencyProvider = concurrencyProvider;
		this.futuresToWaitFor = Collections.emptyList();
		this.callablesToSchedule = callablesToSchedule;
	}

	@Override
	public Collection<Future<?>> call() throws Exception {
		int i = 0;
		int size = futuresToWaitFor.size();
		logger.info(name + ": Waiting for " + size + " futures.");
		for (Future<?> future : futuresToWaitFor) {
			if (logger.isDebugEnabled()) {
				logger.debug(name + ": waiting " + i + "/" + size);
				i++;
			}
			future.get();
		}
		logger.info(name + ": All futures have returned. Scheduling "
				+ callablesToSchedule.size() + " callables.");
		Collection<Future<?>> result = new ArrayList<Future<?>>(
				callablesToSchedule.size());
		for (Callable<?> callable : callablesToSchedule) {
			result.add(concurrencyProvider.schedule(callable));
		}
		logger.info(name + ": Finished.");
		return result;
	}
}
