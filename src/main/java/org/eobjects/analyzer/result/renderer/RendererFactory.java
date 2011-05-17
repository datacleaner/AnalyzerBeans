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
package org.eobjects.analyzer.result.renderer;

import java.util.Collection;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.RendererBeanDescriptor;
import org.eobjects.analyzer.util.ReflectionUtils;

public class RendererFactory {

	private DescriptorProvider descriptorProvider;

	public RendererFactory(DescriptorProvider descriptorProvider) {
		this.descriptorProvider = descriptorProvider;
	}

	public <I extends Renderable, O> Renderer<? super I, ? extends O> getRenderer(I renderable,
			Class<? extends RenderingFormat<O>> renderingFormat) {

		Class<? extends Renderable> renderableType = renderable.getClass();
		RendererBeanDescriptor bestMatchingDescriptor = null;

		Collection<RendererBeanDescriptor> descriptors = descriptorProvider
				.getRendererBeanDescriptorsForRenderingFormat(renderingFormat);
		for (RendererBeanDescriptor descriptor : descriptors) {
			Class<? extends Renderable> renderableType1 = descriptor.getRenderableType();
			if (ReflectionUtils.is(renderableType, renderableType1)) {
				if (bestMatchingDescriptor == null) {
					bestMatchingDescriptor = descriptor;
				} else {
					int dist1 = ReflectionUtils.getHierarchyDistance(renderableType, renderableType1);
					if (dist1 == 0) {
						bestMatchingDescriptor = descriptor;
						break;
					}

					Class<? extends Renderable> renderableType2 = bestMatchingDescriptor.getRenderableType();
					int dist2 = ReflectionUtils.getHierarchyDistance(renderableType, renderableType2);
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
	private <I extends Renderable, O> Renderer<I, O> instantiate(RendererBeanDescriptor descriptor) {
		try {
			Renderer<?, ?> renderer = descriptor.getComponentClass().newInstance();
			return (Renderer<I, O>) renderer;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
