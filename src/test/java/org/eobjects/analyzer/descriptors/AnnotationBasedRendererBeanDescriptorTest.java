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

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.DefaultTextRenderer;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.result.renderer.TextRenderingFormat;

import junit.framework.TestCase;

public class AnnotationBasedRendererBeanDescriptorTest extends TestCase {

	private AnnotationBasedRendererBeanDescriptor descriptor = new AnnotationBasedRendererBeanDescriptor(
			DefaultTextRenderer.class);

	public void testGetRenderingFormat() throws Exception {
		assertEquals(TextRenderingFormat.class, descriptor.getRenderingFormat());
	}

	public void testGetAnalyzerResultType() throws Exception {
		Class<? extends AnalyzerResult> analyzerResultType = descriptor.getRenderableType();
		assertEquals(AnalyzerResult.class, analyzerResultType);

		AnnotationBasedRendererBeanDescriptor desc2 = new AnnotationBasedRendererBeanDescriptor(CrosstabTextRenderer.class);
		assertEquals(CrosstabResult.class, desc2.getRenderableType());
	}

	public void testIsOutputApplicableFor() throws Exception {
		assertTrue(descriptor.isOutputApplicableFor(CharSequence.class));
		assertTrue(descriptor.isOutputApplicableFor(String.class));

		assertFalse(descriptor.isOutputApplicableFor(Number.class));
		assertFalse(descriptor.isOutputApplicableFor(StringBuilder.class));
	}

	public void testInvalidRendererAnnotations() throws Exception {
		try {
			new AnnotationBasedRendererBeanDescriptor(InvalidRenderer1.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals("The renderer output type (class java.lang.Object) is not a valid instance or sub-class "
					+ "of format output type (interface java.lang.CharSequence)", e.getMessage());
		}

		try {
			new AnnotationBasedRendererBeanDescriptor(InvalidRenderer2.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
					"class org.eobjects.analyzer.descriptors.AnnotationBasedRendererBeanDescriptorTest$InvalidRenderer2 doesn't implement the RendererBean annotation",
					e.getMessage());
		}

		try {
			new AnnotationBasedRendererBeanDescriptor(InvalidRenderer3.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
					"Renderer (interface org.eobjects.analyzer.descriptors.AnnotationBasedRendererBeanDescriptorTest$InvalidRenderer3) is not a non-abstract class",
					e.getMessage());
		}

		try {
			new AnnotationBasedRendererBeanDescriptor(InvalidRenderer4.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
					"Rendering format (class org.eobjects.analyzer.descriptors.AnnotationBasedRendererBeanDescriptorTest$InvalidRenderingFormat) is not a non-abstract class",
					e.getMessage());
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static class InvalidRenderer1 implements Renderer<AnalyzerResult, Object> {

		@Override
		public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
			return RendererPrecedence.MEDIUM;
		}

		@Override
		public Object render(AnalyzerResult result) {
			return null;
		}
	}

	public static class InvalidRenderer2 implements Renderer<AnalyzerResult, String> {
		@Override
		public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
			return RendererPrecedence.MEDIUM;
		}

		@Override
		public String render(AnalyzerResult result) {
			return null;
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static interface InvalidRenderer3 extends Renderer<AnalyzerResult, Integer> {
	}

	@RendererBean(InvalidRenderingFormat.class)
	public static class InvalidRenderer4 implements Renderer<AnalyzerResult, Integer> {

		@Override
		public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
			return RendererPrecedence.MEDIUM;
		}

		@Override
		public Integer render(AnalyzerResult result) {
			return null;
		}
	}

	public static abstract class InvalidRenderingFormat implements RenderingFormat<Number> {

		@Override
		public Class<Number> getOutputClass() {
			return Number.class;
		}
	}
}
