package org.eobjects.analyzer.result.renderer;

import java.util.LinkedList;

import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.DataSetResult;
import org.eobjects.analyzer.result.NumberResult;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.data.Row;

import junit.framework.TestCase;

public class RendererFactoryTest extends TestCase {

	public void testGetRenderer() throws Exception {
		RendererFactory rendererFactory = new RendererFactory(TestHelper
				.createAnalyzerBeansConfiguration().getDescriptorProvider());
		Renderer<?, ? extends CharSequence> r;

		r = rendererFactory.getRenderer(new NumberResult(null, 1),
				TextRenderingFormat.class);
		assertEquals(DefaultTextRenderer.class, r.getClass());

		r = rendererFactory.getRenderer(new CrosstabResult(null, null),
				TextRenderingFormat.class);
		assertEquals(CrosstabTextRenderer.class, r.getClass());

		r = rendererFactory.getRenderer(new DataSetResult(
				new LinkedList<Row>(), null), TextRenderingFormat.class);
		assertEquals(DefaultTextRenderer.class, r.getClass());
	}
}
