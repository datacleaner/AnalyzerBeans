package org.eobjects.analyzer.result.renderer;

public class TextRenderingFormat implements RenderingFormat<CharSequence> {

	@Override
	public Class<CharSequence> getOutputClass() {
		return CharSequence.class;
	}
}
