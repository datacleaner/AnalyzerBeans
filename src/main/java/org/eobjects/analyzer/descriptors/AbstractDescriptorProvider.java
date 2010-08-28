package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;

public abstract class AbstractDescriptorProvider implements DescriptorProvider {

	@Override
	public AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(
			String name) {
		if (name != null) {
			for (AnalyzerBeanDescriptor<?> descriptor : getAnalyzerBeanDescriptors()) {
				if (name.equals(descriptor.getDisplayName())) {
					return descriptor;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(
			Class<A> analyzerBeanClass) {
		for (AnalyzerBeanDescriptor<?> descriptor : getAnalyzerBeanDescriptors()) {
			if (descriptor.getBeanClass() == analyzerBeanClass) {
				return (AnalyzerBeanDescriptor<A>) descriptor;
			}
		}
		return null;
	}

	@Override
	public RendererBeanDescriptor getRendererBeanDescriptorForClass(
			Class<? extends Renderer<?, ?>> rendererBeanClass) {
		for (RendererBeanDescriptor descriptor : getRendererBeanDescriptors()) {
			if (descriptor.getBeanClass() == rendererBeanClass) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(
			String name) {
		if (name != null) {
			for (TransformerBeanDescriptor<?> descriptor : getTransformerBeanDescriptors()) {
				if (name.equals(descriptor.getDisplayName())) {
					return descriptor;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Transformer<?>> TransformerBeanDescriptor<T> getTransformerBeanDescriptorForClass(
			Class<T> transformerBeanClass) {
		for (TransformerBeanDescriptor<?> descriptor : getTransformerBeanDescriptors()) {
			if (descriptor.getBeanClass() == transformerBeanClass) {
				return (TransformerBeanDescriptor<T>) descriptor;
			}
		}
		return null;
	}
}
