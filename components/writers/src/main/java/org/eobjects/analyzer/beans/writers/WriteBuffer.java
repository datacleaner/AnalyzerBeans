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
package org.eobjects.analyzer.beans.writers;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.eobjects.metamodel.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a buffering mechanism that enables writing rows periodically instead
 * of instantly.
 * 
 * @author Kasper Sørensen
 */
public final class WriteBuffer {

	private static final Logger logger = LoggerFactory
			.getLogger(WriteBuffer.class);

	private final Queue<Object[]> _buffer;
	private final Action<Queue<Object[]>> _flushAction;

	public WriteBuffer(int bufferSize, Action<Queue<Object[]>> flushAction) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException(
					"Buffer size must be a positive integer");
		}
		_buffer = new ArrayBlockingQueue<Object[]>(bufferSize);
		_flushAction = flushAction;
	}

	public final void addToBuffer(Object[] rowData) {
		while (!_buffer.offer(rowData)) {
			flushBuffer();
		}
	}

	public synchronized final void flushBuffer() {
		if (!_buffer.isEmpty()) {
			logger.info("Flushing {} rows in write buffer", _buffer.size());
			try {
				_flushAction.run(_buffer);
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new IllegalStateException(e);
			}
		}
	}
}
