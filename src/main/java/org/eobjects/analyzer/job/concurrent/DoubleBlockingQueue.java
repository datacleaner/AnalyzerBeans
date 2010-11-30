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

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A capacity-limited blocking queue that also blocks on calls to offer(...).
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
class DoubleBlockingQueue<E> extends LinkedBlockingQueue<E> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DoubleBlockingQueue.class);

	public DoubleBlockingQueue(int capacity) {
		super(capacity);
	}

	public boolean offer(E e) {
		try {
			put(e);
			return true;
		} catch (InterruptedException ex) {
			logger.debug("put(...) was interruped, returning false", ex);
			return false;
		}
	};
}
