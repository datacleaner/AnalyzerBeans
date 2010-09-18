package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.RendererBean;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BeanClassVisitor implements ClassVisitor {

	private final static Logger _logger = LoggerFactory.getLogger(BeanClassVisitor.class);
	private Class<?> _beanClazz;
	private String _name;

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		_name = name;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (isAnnotation(desc, AnalyzerBean.class)
				|| isAnnotation(desc, TransformerBean.class)
				|| isAnnotation(desc, RendererBean.class)) {
			initializeClass();
		}
		return null;
	}

	private boolean isAnnotation(String annotationDesc,
			Class<? extends Annotation> annotationClass) {
		return annotationDesc.indexOf(annotationClass.getName().replace('.',
				'/')) != -1;
	}

	private Class<?> initializeClass() {
		if (_beanClazz == null) {
			String javaName = _name.replace('/', '.');
			try {
				_beanClazz = Class.forName(javaName);
			} catch (ClassNotFoundException e) {
				_logger.error("Could not load class: " + javaName, e);
			}
		}
		return _beanClazz;
	}

	public boolean isAnalyzer() {
		if (_beanClazz != null) {
			return _beanClazz.isAnnotationPresent(AnalyzerBean.class)
					&& ReflectionUtils.is(_beanClazz, Analyzer.class);
		}
		return false;
	}

	public boolean isTransformer() {
		if (_beanClazz != null) {
			return _beanClazz.isAnnotationPresent(TransformerBean.class)
					&& ReflectionUtils.is(_beanClazz, Transformer.class);
		}
		return false;
	}

	public boolean isRenderer() {
		if (_beanClazz != null) {
			return _beanClazz.isAnnotationPresent(RendererBean.class)
					&& ReflectionUtils.is(_beanClazz, Renderer.class);
		}
		return false;
	}

	public Class<?> getBeanClass() {
		return _beanClazz;
	}

	@Override
	public void visitAttribute(Attribute arg0) {
	}

	@Override
	public void visitEnd() {
	}

	@Override
	public FieldVisitor visitField(int arg0, String arg1, String arg2,
			String arg3, Object arg4) {
		return null;
	}

	@Override
	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
	}

	@Override
	public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
			String arg3, String[] arg4) {
		return null;
	}

	@Override
	public void visitOuterClass(String arg0, String arg1, String arg2) {
	}

	@Override
	public void visitSource(String arg0, String arg1) {
	}
}