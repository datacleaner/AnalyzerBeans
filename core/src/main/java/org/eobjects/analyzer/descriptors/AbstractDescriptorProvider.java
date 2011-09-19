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
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.beans.api.Transformer;

/**
 * Abstract descriptor provider implementation that implements most trivial
 * methods.
 * 
 * @author Kasper Sørensen
 */
public abstract class AbstractDescriptorProvider implements DescriptorProvider {

	@Override
	public final AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(String name) {
		if (name != null) {
			for (AnalyzerBeanDescriptor<?> descriptor : getAnalyzerBeanDescriptors()) {
				if (name.equals(descriptor.getDisplayName())) {
					return descriptor;
				}
			}
		}
		return null;
	}
	
	@Override
	public final ExplorerBeanDescriptor<?> getExplorerBeanDescriptorByDisplayName(String name) {
		if (name != null) {
			for (ExplorerBeanDescriptor<?> descriptor : getExplorerBeanDescriptors()) {
				if (name.equals(descriptor.getDisplayName())) {
					return descriptor;
				}
			}
		}
		return null;
	}

	/**
	 * Overridable method for handling (and perhaps discovering) unfound
	 * analyzer descriptors by class.
	 * 
	 * @param analyzerClass
	 * @return
	 */
	protected <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> notFoundAnalyzer(Class<A> analyzerClass) {
		return null;
	}
	
	/**
	 * Overridable method for handling (and perhaps discovering) unfound
	 * explorer descriptors by class.
	 * 
	 * @param explorerClass
	 * @return
	 */
	protected <E extends Explorer<?>> ExplorerBeanDescriptor<E> notFoundExplorer(Class<E> explorerClass) {
		return null;
	}

	/**
	 * Overridable method for handling (and perhaps discovering) unfound
	 * transformer descriptors by class.
	 * 
	 * @param transformerClass
	 * @return
	 */
	protected <A extends Transformer<?>> TransformerBeanDescriptor<A> notFoundTransformer(Class<A> transformerClass) {
		return null;
	}

	/**
	 * Overridable method for handling (and perhaps discovering) unfound
	 * filter descriptors by class.
	 * 
	 * @param filterClass
	 * @return
	 */
	protected FilterBeanDescriptor<?, ?> notFoundFilter(Class<?> filterClass) {
		return null;
	}

	/**
	 * Overridable method for handling (and perhaps discovering) unfound
	 * renderer descriptors by class.
	 * 
	 * @param rendererClass
	 * @return
	 */
	protected RendererBeanDescriptor notFoundRenderer(Class<? extends Renderer<?, ?>> rendererClass) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(
			Class<A> analyzerBeanClass) {
		for (AnalyzerBeanDescriptor<?> descriptor : getAnalyzerBeanDescriptors()) {
			if (descriptor.getComponentClass() == analyzerBeanClass) {
				return (AnalyzerBeanDescriptor<A>) descriptor;
			}
		}
		return notFoundAnalyzer(analyzerBeanClass);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <E extends Explorer<?>> ExplorerBeanDescriptor<E> getExplorerBeanDescriptorForClass(Class<E> explorerClass) {
		for (ExplorerBeanDescriptor<?> descriptor : getExplorerBeanDescriptors()) {
			if (descriptor.getComponentClass() == explorerClass) {
				return (ExplorerBeanDescriptor<E>) descriptor;
			}
		}
		return notFoundExplorer(explorerClass);
	}

	@Override
	public final FilterBeanDescriptor<?, ?> getFilterBeanDescriptorByDisplayName(String name) {
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
	public final <F extends Filter<C>, C extends Enum<C>> FilterBeanDescriptor<F, C> getFilterBeanDescriptorForClass(
			Class<F> filterClass) {
		return (FilterBeanDescriptor<F, C>) getFilterBeanDescriptorForClassUnbounded(filterClass);
	}

	/**
	 * Alternative getter method used when sufficient type-information about the
	 * class is not available.
	 * 
	 * This method is basically a hack to make the compiler happy, see Ticket
	 * #417.
	 * 
	 * @see http://eobjects.org/trac/ticket/417
	 * 
	 * @param clazz
	 * @return
	 */
	protected final FilterBeanDescriptor<?, ?> getFilterBeanDescriptorForClassUnbounded(Class<?> filterClass) {
		for (FilterBeanDescriptor<?, ?> descriptor : getFilterBeanDescriptors()) {
			if (filterClass == descriptor.getComponentClass()) {
				return descriptor;
			}
		}
		return notFoundFilter(filterClass);
	}

	@Override
	public final RendererBeanDescriptor getRendererBeanDescriptorForClass(Class<? extends Renderer<?, ?>> rendererBeanClass) {
		for (RendererBeanDescriptor descriptor : getRendererBeanDescriptors()) {
			if (descriptor.getComponentClass() == rendererBeanClass) {
				return descriptor;
			}
		}
		return notFoundRenderer(rendererBeanClass);
	}

	@Override
	public final TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(String name) {
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
	public final <T extends Transformer<?>> TransformerBeanDescriptor<T> getTransformerBeanDescriptorForClass(
			Class<T> transformerClass) {
		for (TransformerBeanDescriptor<?> descriptor : getTransformerBeanDescriptors()) {
			if (descriptor.getComponentClass() == transformerClass) {
				return (TransformerBeanDescriptor<T>) descriptor;
			}
		}
		return notFoundTransformer(transformerClass);
	}

	@Override
	public final Collection<RendererBeanDescriptor> getRendererBeanDescriptorsForRenderingFormat(
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
