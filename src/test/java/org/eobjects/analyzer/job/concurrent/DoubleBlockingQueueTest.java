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

import junit.framework.TestCase;

public class DoubleBlockingQueueTest extends TestCase {

	public void testOffer() throws Exception {
		DoubleBlockingQueue<String> q = new DoubleBlockingQueue<String>(4);
		assertTrue(q.offer("hi"));
		assertTrue(q.offer("hello"));
		assertTrue(q.offer("howdy"));
		assertTrue(q.offer("greetings"));

		// the next offer(...) call will block. Test it with multiple threads
		// somehow?
	}
}
