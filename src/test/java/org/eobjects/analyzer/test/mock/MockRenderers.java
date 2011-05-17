package org.eobjects.analyzer.test.mock;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderable;
import org.eobjects.analyzer.result.renderer.TextRenderingFormat;

public class MockRenderers {

	public static class RenderableString implements Renderable {
		private final String str;

		public RenderableString(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static class FooPrecedenceRenderer implements Renderer<RenderableString, String> {
		@Override
		public RendererPrecedence getPrecedence(RenderableString renderable) {
			if (renderable.toString().equals("foo")) {
				return RendererPrecedence.HIGHEST;
			}
			return RendererPrecedence.HIGH;
		}

		@Override
		public String render(RenderableString renderable) {
			return "high";
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static class BarPrecedenceRenderer implements Renderer<RenderableString, String> {
		@Override
		public RendererPrecedence getPrecedence(RenderableString renderable) {
			if (renderable.toString().equals("bar")) {
				return RendererPrecedence.HIGHEST;
			}
			return RendererPrecedence.LOW;
		}

		@Override
		public String render(RenderableString renderable) {
			return "low";
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static class ConditionalPrecedenceRenderer implements Renderer<RenderableString, String> {

		@Override
		public RendererPrecedence getPrecedence(RenderableString renderable) {
			// renderable's toString() method should return the name of the
			// precedence (can also be used for testing exceptions in
			// resolving).
			return RendererPrecedence.valueOf(renderable.toString());
		}

		@Override
		public String render(RenderableString renderable) {
			return "low";
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

	public static abstract class InvalidRenderingFormat implements RenderingFormat<Number> {

		@Override
		public Class<Number> getOutputClass() {
			return Number.class;
		}
	}
}
