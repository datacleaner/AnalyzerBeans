package org.eobjects.analyzer.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;

/**
 * A simple descriptor provider with a method signature suitable externalizing
 * class names of analyzer and transformer beans. For example, if you're using
 * the Spring Framework you initialize this descriptor provider as follows:
 * 
 * <pre>
 * &lt;bean id="descriptorProvider" class="org.eobjects.analyzer.descriptors.SimpleDescriptorProvider"&gt;
 *   &lt;property name="analyzerClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.StringAnalyzer&lt;/value&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="transformerClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.TokenizerTransformer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="rendererClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.eobjects.analyzer.result.renderer.DefaultTextRenderer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Kasper Sørensen
 */
public class SimpleDescriptorProvider extends AbstractDescriptorProvider {

	private List<AnalyzerBeanDescriptor<?>> _analyzerBeanDescriptors = new ArrayList<AnalyzerBeanDescriptor<?>>();
	private List<TransformerBeanDescriptor<?>> _transformerBeanDescriptors = new ArrayList<TransformerBeanDescriptor<?>>();
	private List<RendererBeanDescriptor> _rendererBeanDescriptors = new ArrayList<RendererBeanDescriptor>();
	private List<FilterBeanDescriptor<?, ?>> _filterBeanDescriptors = new ArrayList<FilterBeanDescriptor<?, ?>>();

	public SimpleDescriptorProvider() {
	}

	public void addAnalyzerBeanDescriptor(AnalyzerBeanDescriptor<?> analyzerBeanDescriptor) {
		_analyzerBeanDescriptors.add(analyzerBeanDescriptor);
	}

	public void addTransformerBeanDescriptor(TransformerBeanDescriptor<?> transformerBeanDescriptor) {
		_transformerBeanDescriptors.add(transformerBeanDescriptor);
	}

	public void addRendererBeanDescriptor(RendererBeanDescriptor rendererBeanDescriptor) {
		_rendererBeanDescriptors.add(rendererBeanDescriptor);
	}

	public void addFilterBeanDescriptor(FilterBeanDescriptor<? extends Filter<?>, ?> descriptor) {
		_filterBeanDescriptors.add(descriptor);
	}

	@Override
	public List<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors() {
		return _analyzerBeanDescriptors;
	}

	public void setAnalyzerBeanDescriptors(List<AnalyzerBeanDescriptor<?>> descriptors) {
		_analyzerBeanDescriptors = descriptors;
	}

	@Override
	public List<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors() {
		return _transformerBeanDescriptors;
	}

	public void setTransformerBeanDescriptors(List<TransformerBeanDescriptor<?>> transformerBeanDescriptors) {
		_transformerBeanDescriptors = transformerBeanDescriptors;
	}

	@Override
	public List<RendererBeanDescriptor> getRendererBeanDescriptors() {
		return _rendererBeanDescriptors;
	}

	public void setRendererBeanDescriptors(List<RendererBeanDescriptor> rendererBeanDescriptors) {
		_rendererBeanDescriptors = rendererBeanDescriptors;
	}

	@Override
	public Collection<FilterBeanDescriptor<?, ?>> getFilterBeanDescriptors() {
		return _filterBeanDescriptors;
	}

	public void setFilterBeanDescriptors(List<FilterBeanDescriptor<?, ?>> filterBeanDescriptors) {
		_filterBeanDescriptors = filterBeanDescriptors;
	}

	public void setAnalyzerClassNames(Collection<String> classNames) throws ClassNotFoundException {
		for (String className : classNames) {
			@SuppressWarnings("unchecked")
			Class<? extends Analyzer<?>> c = (Class<? extends Analyzer<?>>) Class.forName(className);
			AnalyzerBeanDescriptor<?> descriptor = getAnalyzerBeanDescriptorForClass(c);
			if (descriptor == null) {
				addAnalyzerBeanDescriptor(AnnotationBasedAnalyzerBeanDescriptor.create(c));
			}
		}
	}

	public void setTransformerClassNames(Collection<String> classNames) throws ClassNotFoundException {
		for (String className : classNames) {
			@SuppressWarnings("unchecked")
			Class<? extends Transformer<?>> c = (Class<? extends Transformer<?>>) Class.forName(className);
			TransformerBeanDescriptor<?> descriptor = getTransformerBeanDescriptorForClass(c);
			if (descriptor == null) {
				addTransformerBeanDescriptor(AnnotationBasedTransformerBeanDescriptor.create(c));
			}
		}
	}

	public void setRendererClassNames(Collection<String> classNames) throws ClassNotFoundException {
		for (String className : classNames) {
			@SuppressWarnings("unchecked")
			Class<? extends Renderer<?, ?>> c = (Class<? extends Renderer<?, ?>>) Class.forName(className);
			RendererBeanDescriptor descriptor = getRendererBeanDescriptorForClass(c);
			if (descriptor == null) {
				addRendererBeanDescriptor(new AnnotationBasedRendererBeanDescriptor(c));
			}
		}
	}

	public void setFilterClassNames(Collection<String> classNames) throws ClassNotFoundException {
		for (String className : classNames) {
			@SuppressWarnings("unchecked")
			Class<? extends Filter<?>> c = (Class<? extends Filter<?>>) Class.forName(className);
			FilterBeanDescriptor<?, ?> descriptor = getFilterBeanDescriptorForClass(c);
			if (descriptor == null) {
				addFilterBeanDescriptor(AnnotationBasedFilterBeanDescriptor.create(c));
			}
		}
	}
}
