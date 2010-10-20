package org.eobjects.analyzer.result.renderer;

public class HtmlRenderingFormat implements RenderingFormat<String> {

	@Override
	public Class<String> getOutputClass() {
		return String.class;
	}
}
