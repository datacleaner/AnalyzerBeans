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
package org.eobjects.analyzer.connection;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.metamodel.DataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract pooled {@link DatastoreConnection} that is aware of the amount of
 * times it has been acquired and closed. It encapsulates the closing logic,
 * making sure that it will only close if all usages of the datastore are
 * closed.
 * 
 * @author Kasper Sørensen
 */
public abstract class UsageAwareDatastoreConnection<E extends DataContext> implements DatastoreConnection {

	private static final Logger logger = LoggerFactory.getLogger(UsageAwareDatastoreConnection.class);

	private final AtomicInteger _usageCount = new AtomicInteger(1);
	private final Datastore _datastore;
	private boolean _closed = false;

	public UsageAwareDatastoreConnection(Datastore datastore) {
		_datastore = datastore;
		if (logger.isDebugEnabled()) {
			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
			logger.debug("{} instantiated by:", this);
			for (int i = 0; i < stackTrace.length && i < 7; i++) {
				StackTraceElement ste = stackTrace[i];
				logger.debug(" - {} @ line {}", ste.getClassName(), ste.getLineNumber());
			}
		}
	}

	public final void incrementUsageCount() {
		int count = _usageCount.incrementAndGet();
		logger.info("Usage incremented to {} for {}", count, this);

		if (logger.isDebugEnabled()) {
			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
			logger.debug("Incremented usage by:");
			for (int i = 0; i < stackTrace.length && i < 7; i++) {
				StackTraceElement ste = stackTrace[i];
				logger.debug(" - {} @ line {}", ste.getClassName(), ste.getLineNumber());
			}
		}
	}

	@Override
	public abstract E getDataContext();

	public synchronized boolean isClosed() {
		return _closed;
	}

	@Override
	public synchronized final void close() {
		int users = _usageCount.decrementAndGet();
		logger.info("Method close() invoked, usage decremented to {} for {}", users, this);
		if (users == 0) {
			logger.info("Closing {}", this);
			_closed = true;
			closeInternal();
		}
	}

	/**
	 * Subclasses should implement this method to do the actual closing logic of
	 * the datacontext
	 */
	protected abstract void closeInternal();

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (!isClosed()) {
			if (logger.isWarnEnabled()) {

				logger.warn(
						"Method finalize() invoked but not all usages closed ({} remaining) (for {}). Closing DatastoreConnection.",
						_usageCount.get(), this);
			}
			// in case of gc, also do the closing
			closeInternal();
		}
	}

	@Override
	public String toString() {
		return "UsageAwareDatastoreConnection[datastore=" + getDatastoreName() + "]";
	}

	@Override
	public final Datastore getDatastore() {
		return _datastore;
	}

	private String getDatastoreName() {
		if (_datastore != null) {
			return _datastore.getName();
		}
		return "<null>";
	}

	/**
	 * Gets the amount of usages this datacontext provider currently has.
	 * 
	 * @return
	 */
	public int getUsageCount() {
		return _usageCount.get();
	}
}
