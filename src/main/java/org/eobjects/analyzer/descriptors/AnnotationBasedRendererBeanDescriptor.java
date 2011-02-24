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

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RenderingFormat;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnnotationBasedRendererBeanDescriptor implements RendererBeanDescriptor {

	private static final Logger logger = LoggerFactory.getLogger(AnnotationBasedRendererBeanDescriptor.class);

	private Class<? extends Renderer<?, ?>> _componentClass;
	private RendererBean _rendererBeanAnnotation;
	private Class<? extends RenderingFormat<?>> _renderingFormat;
	private Class<?> _formatOutputType = null;
	private Class<? extends AnalyzerResult> _rendererInputType = null;
	private Class<?> _rendererOutputType = null;

	@SuppressWarnings("unchecked")
	public AnnotationBasedRendererBeanDescriptor(Class<? extends Renderer<?, ?>> beanClass) throws DescriptorException {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass cannot be null");
		}
		_componentClass = beanClass;
		_rendererBeanAnnotation = _componentClass.getAnnotation(RendererBean.class);
		if (_rendererBeanAnnotation == null) {
			throw new DescriptorException(beanClass + " doesn't implement the RendererBean annotation");
		}

		if (_componentClass.isInterface() || Modifier.isAbstract(_componentClass.getModifiers())) {
			throw new DescriptorException("Renderer (" + _componentClass + ") is not a non-abstract class");
		}

		_renderingFormat = _rendererBeanAnnotation.value();
		if (_renderingFormat == null || _renderingFormat.isInterface()
				|| Modifier.isAbstract(_renderingFormat.getModifiers())) {
			throw new DescriptorException("Rendering format (" + _renderingFormat + ") is not a non-abstract class");
		}

		_formatOutputType = ReflectionUtils.getTypeParameter(_renderingFormat, RenderingFormat.class, 0);
		logger.debug("Found format output type: {}", _formatOutputType);

		if (_formatOutputType == null) {
			throw new DescriptorException("Could not determine output type of rendering format: " + _renderingFormat);
		}

		_rendererInputType = (Class<? extends AnalyzerResult>) ReflectionUtils.getTypeParameter(_componentClass,
				Renderer.class, 0);
		logger.debug("Found renderer input type: {}", _rendererInputType);
		_rendererOutputType = ReflectionUtils.getTypeParameter(_componentClass, Renderer.class, 1);
		logger.debug("Found renderer output type: {}", _rendererOutputType);

		if (_rendererOutputType == null) {
			throw new DescriptorException("Could not determine output type of renderer: " + _componentClass);
		}

		if (!ReflectionUtils.is(_rendererOutputType, _formatOutputType)) {
			throw new DescriptorException("The renderer output type (" + _rendererOutputType
					+ ") is not a valid instance or sub-class of format output type (" + _formatOutputType + ")");
		}
	}

	@Override
	public Class<? extends Renderer<?, ?>> getComponentClass() {
		return _componentClass;
	}

	@Override
	public Class<? extends RenderingFormat<?>> getRenderingFormat() {
		return _renderingFormat;
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return CollectionUtils.set(_componentClass.getAnnotations());
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return _componentClass.getAnnotation(annotationClass);
	}

	@Override
	public int compareTo(RendererBeanDescriptor o) {
		if (o == null) {
			return 1;
		}
		Class<?> otherBeanClass = o.getComponentClass();
		if (otherBeanClass == null) {
			return 1;
		}
		String thisBeanClassName = this.getComponentClass().toString();
		String thatBeanClassName = otherBeanClass.toString();
		return thisBeanClassName.compareTo(thatBeanClassName);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + _componentClass.getName() + "]";
	}

	@Override
	public int hashCode() {
		return _componentClass.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass() == AnnotationBasedRendererBeanDescriptor.class) {
			AnnotationBasedRendererBeanDescriptor that = (AnnotationBasedRendererBeanDescriptor) obj;
			return this._componentClass.equals(that._componentClass);
		}
		return false;
	}

	public boolean isOutputApplicableFor(Class<?> requiredClass) {
		if (!ReflectionUtils.is(requiredClass, _formatOutputType)) {
			logger.debug("{} is not applicable to the format output type: {}", requiredClass, _formatOutputType);
			return false;
		}

		boolean result = ReflectionUtils.is(_rendererOutputType, requiredClass);

		if (!result) {
			logger.debug("{} is not applicable to the renderer output type: {}", requiredClass, _rendererOutputType);
		}

		return result;
	}

	@Override
	public Class<? extends AnalyzerResult> getAnalyzerResultType() {
		return _rendererInputType;
	}
}
