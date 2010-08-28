package org.eobjects.analyzer.descriptors;

import java.util.Collection;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;

/**
 * An interface for components that provide descriptors for analyzer beans.
 * 
 * @author Kasper Sørensen
 */
public interface DescriptorProvider {

	public Collection<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors();

	public <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(
			Class<A> analyzerBeanClass);

	public AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(
			String name);

	public Collection<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors();

	public <T extends Transformer<?>> TransformerBeanDescriptor<T> getTransformerBeanDescriptorForClass(
			Class<T> transformerBeanClass);

	public TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(
			String name);

	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors();

	public RendererBeanDescriptor getRendererBeanDescriptorForClass(
			Class<? extends Renderer<?, ?>> rendererBeanClass);
}
