package org.eobjects.analyzer.result.renderer;

import java.util.Collection;

import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.RendererBeanDescriptor;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.ReflectionUtils;

public class RendererFactory {

	private DescriptorProvider descriptorProvider;

	public RendererFactory(DescriptorProvider descriptorProvider) {
		this.descriptorProvider = descriptorProvider;
	}

	public <I extends AnalyzerResult, O> Renderer<? super I, ? extends O> getRenderer(I analyzerResult,
			Class<? extends RenderingFormat<O>> renderingFormat) {

		Class<? extends AnalyzerResult> analyzerResultType = analyzerResult.getClass();
		RendererBeanDescriptor bestMatchingDescriptor = null;

		Collection<RendererBeanDescriptor> descriptors = descriptorProvider
				.getRendererBeanDescriptorsForRenderingFormat(renderingFormat);
		for (RendererBeanDescriptor descriptor : descriptors) {
			Class<? extends AnalyzerResult> analyzerResultType1 = descriptor.getAnalyzerResultType();
			if (ReflectionUtils.is(analyzerResultType, analyzerResultType1)) {
				if (bestMatchingDescriptor == null) {
					bestMatchingDescriptor = descriptor;
				} else {
					int dist1 = ReflectionUtils.getHierarchyDistance(analyzerResultType, analyzerResultType1);
					if (dist1 == 0) {
						bestMatchingDescriptor = descriptor;
						break;
					}
					
					Class<? extends AnalyzerResult> analyzerResultType2 = bestMatchingDescriptor.getAnalyzerResultType();
					int dist2 = ReflectionUtils.getHierarchyDistance(analyzerResultType, analyzerResultType2);
					if (dist1 < dist2) {
						bestMatchingDescriptor = descriptor;
					}
				}
			}
		}

		if (bestMatchingDescriptor == null) {
			return null;
		}

		return instantiate(bestMatchingDescriptor);
	}

	@SuppressWarnings("unchecked")
	private <I extends AnalyzerResult, O> Renderer<I, O> instantiate(RendererBeanDescriptor descriptor) {
		try {
			Renderer<?, ?> renderer = descriptor.getBeanClass().newInstance();
			return (Renderer<I, O>) renderer;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
