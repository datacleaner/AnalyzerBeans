package org.eobjects.analyzer.result.renderer;

import org.eobjects.analyzer.result.NumberResult;

import junit.framework.TestCase;

public class DefaultTextRendererTest extends TestCase {

	public void testSimpleRendering() throws Exception {
		DefaultTextRenderer r = new DefaultTextRenderer();
		assertEquals("1234", r.render(new NumberResult(1234)));
	}
}
