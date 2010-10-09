package org.eobjects.analyzer.descriptors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RenderingFormat;

public abstract class AbstractDescriptorProvider implements DescriptorProvider {

	@Override
	public AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(String name) {
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
	public <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(Class<A> analyzerBeanClass) {
		for (AnalyzerBeanDescriptor<?> descriptor : getAnalyzerBeanDescriptors()) {
			if (descriptor.getBeanClass() == analyzerBeanClass) {
				return (AnalyzerBeanDescriptor<A>) descriptor;
			}
		}
		return null;
	}

	@Override
	public FilterBeanDescriptor<?, ?> getFilterBeanDescriptorByDisplayName(String name) {
		if (name != null) {
			for (FilterBeanDescriptor<?, ?> descriptor : getFilterBeanDescriptors()) {
				if (name.equals(descriptor.getDisplayName())) {
					return descriptor;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <F extends Filter<C>, C extends Enum<C>> FilterBeanDescriptor<F, C> getFilterBeanDescriptorForClass(Class<F> filterClass) {
		for (FilterBeanDescriptor<?, ?> descriptor : getFilterBeanDescriptors()) {
			if (filterClass == descriptor.getBeanClass()) {
				return (FilterBeanDescriptor<F, C>) descriptor;
			}
		}
		return null;
	}

	@Override
	public RendererBeanDescriptor getRendererBeanDescriptorForClass(Class<? extends Renderer<?, ?>> rendererBeanClass) {
		for (RendererBeanDescriptor descriptor : getRendererBeanDescriptors()) {
			if (descriptor.getBeanClass() == rendererBeanClass) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(String name) {
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

	@Override
	public Collection<RendererBeanDescriptor> getRendererBeanDescriptorsForRenderingFormat(
			Class<? extends RenderingFormat<?>> renderingFormat) {
		Set<RendererBeanDescriptor> result = new HashSet<RendererBeanDescriptor>();
		Collection<RendererBeanDescriptor> descriptors = getRendererBeanDescriptors();
		for (RendererBeanDescriptor descriptor : descriptors) {
			Class<? extends RenderingFormat<?>> descriptorsRenderingFormat = descriptor.getRenderingFormat();
			if (descriptorsRenderingFormat == renderingFormat) {
				result.add(descriptor);
			}
		}
		return result;
	}
}
