package org.eobjects.analyzer.job;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A very simple ID generator that generates id with a prefix and an
 * incrementing suffix.
 * 
 * @author Kasper Sørensen
 */
public final class PrefixedIdGenerator implements IdGenerator {

	private final String prefix;
	private final AtomicInteger counter;

	public PrefixedIdGenerator(String prefix) {
		this.prefix = prefix;
		this.counter = new AtomicInteger(0);
	}

	@Override
	public String nextId() {
		int i = counter.incrementAndGet();
		return prefix + '-' + i;
	}

}
