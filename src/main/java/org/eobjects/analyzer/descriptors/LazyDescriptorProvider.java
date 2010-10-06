package org.eobjects.analyzer.descriptors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RenderingFormat;

public class LazyDescriptorProvider implements DescriptorProvider {

	private Map<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>> _analyzerBeanDescriptors = new HashMap<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>>();
	private Map<Class<? extends Filter<?>>, FilterBeanDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<Class<? extends Filter<?>>, FilterBeanDescriptor<?, ?>>();
	private Map<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>> _transformerBeanDescriptors = new HashMap<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>>();
	private Map<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor> _rendererBeanDescriptors = new HashMap<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor>();

	@SuppressWarnings("unchecked")
	@Override
	public <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(Class<A> analyzerBeanClass) {
		AnalyzerBeanDescriptor<?> descriptor = _analyzerBeanDescriptors.get(analyzerBeanClass);
		if (descriptor == null) {
			descriptor = AnnotationBasedAnalyzerBeanDescriptor.create(analyzerBeanClass);
			_analyzerBeanDescriptors.put(analyzerBeanClass, descriptor);
		}
		return (AnalyzerBeanDescriptor<A>) descriptor;
	}

	@Override
	public Collection<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors() {
		return Collections.unmodifiableCollection(_analyzerBeanDescriptors.values());
	}

	@Override
	public Collection<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors() {
		return Collections.unmodifiableCollection(_transformerBeanDescriptors.values());
	}

	@Override
	public AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(String name) {
		Collection<AnalyzerBeanDescriptor<?>> analyzerBeanDescriptors = getAnalyzerBeanDescriptors();
		for (AnalyzerBeanDescriptor<?> analyzerBeanDescriptor : analyzerBeanDescriptors) {
			if (name.equals(analyzerBeanDescriptor.getDisplayName())) {
				return analyzerBeanDescriptor;
			}
		}
		return null;
	}

	@Override
	public TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(String name) {
		Collection<TransformerBeanDescriptor<?>> transformerBeanDescriptors = getTransformerBeanDescriptors();
		for (TransformerBeanDescriptor<?> transformerBeanDescriptor : transformerBeanDescriptors) {
			if (name.equals(transformerBeanDescriptor.getDisplayName())) {
				return transformerBeanDescriptor;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Transformer<?>> TransformerBeanDescriptor<T> getTransformerBeanDescriptorForClass(
			Class<T> transformerBeanClass) {
		TransformerBeanDescriptor<?> descriptor = _transformerBeanDescriptors.get(transformerBeanClass);
		if (descriptor == null) {
			descriptor = AnnotationBasedTransformerBeanDescriptor.create(transformerBeanClass);
			_transformerBeanDescriptors.put(transformerBeanClass, descriptor);
		}
		return (TransformerBeanDescriptor<T>) descriptor;
	}

	@Override
	public RendererBeanDescriptor getRendererBeanDescriptorForClass(Class<? extends Renderer<?, ?>> rendererBeanClass) {
		return _rendererBeanDescriptors.get(rendererBeanClass);
	}

	@Override
	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors() {
		return Collections.unmodifiableCollection(_rendererBeanDescriptors.values());
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

	@Override
	public Collection<FilterBeanDescriptor<?, ?>> getFilterBeanDescriptors() {
		return Collections.unmodifiableCollection(_filterBeanDescriptors.values());
	}

	@Override
	public FilterBeanDescriptor<?, ?> getFilterBeanDescriptorByDisplayName(String name) {
		Collection<FilterBeanDescriptor<?, ?>> filterBeanDescriptors = getFilterBeanDescriptors();
		for (FilterBeanDescriptor<?, ?> filterBeanDescriptor : filterBeanDescriptors) {
			if (name.equals(filterBeanDescriptor.getDisplayName())) {
				return filterBeanDescriptor;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F extends Filter<?>> FilterBeanDescriptor<F, ?> getFilterBeanDescriptorForClass(Class<F> filterClass) {
		FilterBeanDescriptor<?, ?> descriptor = _filterBeanDescriptors.get(filterClass);
		if (descriptor == null) {
			Class<? extends Filter<?>> fc = filterClass;
			descriptor = AnnotationBasedFilterBeanDescriptor.create(fc);
		}
		return (FilterBeanDescriptor<F, ?>) descriptor;
	}
}
