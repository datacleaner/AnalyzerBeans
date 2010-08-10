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

	public Collection<AnalyzerBeanDescriptor> getAnalyzerBeanDescriptors();

	public AnalyzerBeanDescriptor getAnalyzerBeanDescriptorForClass(
			Class<? extends Analyzer<?>> analyzerBeanClass);

	public Collection<TransformerBeanDescriptor> getTransformerBeanDescriptors();

	public TransformerBeanDescriptor getTransformerBeanDescriptorForClass(
			Class<? extends Transformer<?>> transformerBeanClass);

	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors();

	public RendererBeanDescriptor getRendererBeanDescriptorForClass(
			Class<? extends Renderer<?, ?>> rendererBeanClass);
}
