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
package org.eobjects.analyzer.result.renderer;

import java.util.LinkedList;

import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.DataSetResult;
import org.eobjects.analyzer.result.NumberResult;
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.test.TestHelper;

import org.eobjects.metamodel.data.Row;

import junit.framework.TestCase;

public class RendererFactoryTest extends TestCase {

	public void testGetRenderer() throws Exception {
		RendererFactory rendererFactory = new RendererFactory(TestHelper.createAnalyzerBeansConfiguration()
				.getDescriptorProvider());
		Renderer<?, ? extends CharSequence> r;

		r = rendererFactory.getRenderer(new NumberResult(1), TextRenderingFormat.class);
		assertEquals(DefaultTextRenderer.class, r.getClass());

		r = rendererFactory.getRenderer(new CrosstabResult(null), TextRenderingFormat.class);
		assertEquals(CrosstabTextRenderer.class, r.getClass());

		r = rendererFactory.getRenderer(new PatternFinderResult(null, null), TextRenderingFormat.class);
		assertEquals(CrosstabTextRenderer.class, r.getClass());

		r = rendererFactory.getRenderer(new DataSetResult(new LinkedList<Row>()), TextRenderingFormat.class);
		assertEquals(DefaultTextRenderer.class, r.getClass());
	}
}
