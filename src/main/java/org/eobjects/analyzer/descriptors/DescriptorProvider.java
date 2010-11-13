/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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

	public <F extends Filter<C>, C extends Enum<C>> FilterBeanDescriptor<F, C> getFilterBeanDescriptorForClass(
			Class<F> filterClass);

	public FilterBeanDescriptor<?, ?> getFilterBeanDescriptorByDisplayName(String name);

	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors();

	public RendererBeanDescriptor getRendererBeanDescriptorForClass(Class<? extends Renderer<?, ?>> rendererBeanClass);

	public Collection<RendererBeanDescriptor> getRendererBeanDescriptorsForRenderingFormat(
			Class<? extends RenderingFormat<?>> renderingFormat);
}
