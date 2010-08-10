package org.eobjects.analyzer.result.renderer;

import javax.swing.JComponent;

public class SwingRenderingFormat implements RenderingFormat<JComponent> {

	@Override
	public Class<JComponent> getOutputClass() {
		return JComponent.class;
	}
}
