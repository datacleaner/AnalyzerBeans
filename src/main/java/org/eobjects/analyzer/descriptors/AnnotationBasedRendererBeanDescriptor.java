package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RenderingFormat;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnnotationBasedRendererBeanDescriptor implements
		RendererBeanDescriptor {

	private static final Logger logger = LoggerFactory
			.getLogger(AnnotationBasedRendererBeanDescriptor.class);

	private Class<? extends Renderer<?, ?>> _beanClass;
	private RendererBean _rendererBeanAnnotation;
	private Class<? extends RenderingFormat<?>> _renderingFormat;
	private Class<?> _formatOutputType = null;
	private Class<? extends AnalyzerResult> _rendererInputType = null;
	private Class<?> _rendererOutputType = null;

	@SuppressWarnings("unchecked")
	public AnnotationBasedRendererBeanDescriptor(
			Class<? extends Renderer<?, ?>> beanClass)
			throws DescriptorException {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass cannot be null");
		}
		_beanClass = beanClass;
		_rendererBeanAnnotation = _beanClass.getAnnotation(RendererBean.class);
		if (_rendererBeanAnnotation == null) {
			throw new DescriptorException(beanClass
					+ " doesn't implement the RendererBean annotation");
		}

		if (_beanClass.isInterface()
				|| Modifier.isAbstract(_beanClass.getModifiers())) {
			throw new DescriptorException("Renderer (" + _beanClass
					+ ") is not a non-abstract class");
		}

		_renderingFormat = _rendererBeanAnnotation.value();
		if (_renderingFormat == null || _renderingFormat.isInterface()
				|| Modifier.isAbstract(_renderingFormat.getModifiers())) {
			throw new DescriptorException("Rendering format ("
					+ _renderingFormat + ") is not a non-abstract class");
		}

		Type[] genericInterfaces = _renderingFormat.getGenericInterfaces();
		for (Type type : genericInterfaces) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				if (pType.getRawType() == RenderingFormat.class) {
					_formatOutputType = ReflectionUtils.getTypeParameter(pType,
							0);
					logger.debug("Found format output type: {}",
							_formatOutputType);
					break;
				}
			}
		}

		if (_formatOutputType == null) {
			throw new DescriptorException(
					"Could not determine output type of rendering format: "
							+ _renderingFormat);
		}

		genericInterfaces = _beanClass.getGenericInterfaces();
		for (Type type : genericInterfaces) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				if (pType.getRawType() == Renderer.class) {
					_rendererInputType = (Class<? extends AnalyzerResult>) ReflectionUtils
							.getTypeParameter(pType, 0);
					logger.debug("Found renderer input type: {}",
							_rendererInputType);
					_rendererOutputType = ReflectionUtils.getTypeParameter(
							pType, 1);
					logger.debug("Found renderer output type: {}",
							_rendererOutputType);
					break;
				}
			}
		}

		if (_rendererOutputType == null) {
			throw new DescriptorException(
					"Could not determine output type of renderer: "
							+ _beanClass);
		}

		if (!ReflectionUtils.is(_rendererOutputType, _formatOutputType)) {
			throw new DescriptorException(
					"The renderer output type ("
							+ _rendererOutputType
							+ ") is not a valid instance or sub-class of format output type ("
							+ _formatOutputType + ")");
		}
	}

	@Override
	public Class<? extends Renderer<?, ?>> getBeanClass() {
		return _beanClass;
	}

	@Override
	public Class<? extends RenderingFormat<?>> getRenderingFormat() {
		return _renderingFormat;
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return CollectionUtils.set(_beanClass.getAnnotations());
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return _beanClass.getAnnotation(annotationClass);
	}

	@Override
	public int compareTo(RendererBeanDescriptor o) {
		if (o == null) {
			return 1;
		}
		Class<?> otherBeanClass = o.getBeanClass();
		if (otherBeanClass == null) {
			return 1;
		}
		String thisBeanClassName = this.getBeanClass().toString();
		String thatBeanClassName = otherBeanClass.toString();
		return thisBeanClassName.compareTo(thatBeanClassName);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[beanClass="
				+ _beanClass.getName() + "]";
	}

	@Override
	public int hashCode() {
		return _beanClass.hashCode();
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
			return this._beanClass.equals(that._beanClass);
		}
		return false;
	}

	public boolean isOutputApplicableFor(Class<?> inquiredClass) {
		if (!ReflectionUtils.is(inquiredClass, _formatOutputType)) {
			logger.debug("{} is not applicable to the format output type: {}",
					inquiredClass, _formatOutputType);
			return false;
		}

		boolean result = ReflectionUtils.is(_rendererOutputType, inquiredClass);

		if (!result) {
			logger.debug(
					"{} is not applicable to the renderer output type: {}",
					inquiredClass, _rendererOutputType);
		}

		return result;
	}

	@Override
	public Class<? extends AnalyzerResult> getAnalyzerResultType() {
		return _rendererInputType;
	}
}
