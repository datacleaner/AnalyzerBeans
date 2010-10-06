package org.eobjects.analyzer.descriptors;

import java.util.Collection;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RenderingFormat;

/**
 * An interface for components that provide descriptors for analyzer beans.
 * 
 * @author Kasper Sørensen
 */
public interface DescriptorProvider {

	public Collection<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors();

	public <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(Class<A> analyzerClass);

	public AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(String name);

	public Collection<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors();

	public <T extends Transformer<?>> TransformerBeanDescriptor<T> getTransformerBeanDescriptorForClass(
			Class<T> transformerClass);

	public TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(String name);

	public Collection<FilterBeanDescriptor<?, ?>> getFilterBeanDescriptors();

	public <F extends Filter<?>> FilterBeanDescriptor<F, ?> getFilterBeanDescriptorForClass(Class<F> filterClass);

	public FilterBeanDescriptor<?, ?> getFilterBeanDescriptorByDisplayName(String name);

	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors();

	public RendererBeanDescriptor getRendererBeanDescriptorForClass(Class<? extends Renderer<?, ?>> rendererBeanClass);

	public Collection<RendererBeanDescriptor> getRendererBeanDescriptorsForRenderingFormat(
			Class<? extends RenderingFormat<?>> renderingFormat);
}
