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
package org.eobjects.analyzer.descriptors;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock;

public class AnnotationBasedExplorerBeanDescriptorTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ExploringAnalyzerMock.clearInstances();
	}

	public void testExploringType() throws Exception {
		ExplorerBeanDescriptor<?> descriptor = Descriptors.ofExplorer(ExploringAnalyzerMock.class);

		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
		Iterator<ConfiguredPropertyDescriptor> it = configuredProperties.iterator();
		assertTrue(it.hasNext());
		assertEquals("Configured1", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Configured2", it.next().getName());
		assertFalse(it.hasNext());
	}
}
