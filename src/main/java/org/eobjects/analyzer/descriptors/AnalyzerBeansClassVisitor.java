package org.eobjects.analyzer.descriptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class AnalyzerBeansClassVisitor implements ClassVisitor {

	private final static Log _log = LogFactory
			.getLog(AnalyzerBeansClassVisitor.class);
	public static final String ANALYZER_DESC = 'L' + AnalyzerBean.class
			.getCanonicalName().replace('.', '/') + ';';
	private Class<?> _analyzerClazz;
	private String _name;

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		_name = name;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (desc.indexOf(AnalyzerBean.class.getName().replace('.', '/')) != -1) {
			initializeClass();
		}
		return null;
	}

	private Class<?> initializeClass() {
		if (_analyzerClazz == null) {
			String javaName = _name.replace('/', '.');
			try {
				_analyzerClazz = Class.forName(javaName);
			} catch (ClassNotFoundException e) {
				_log.fatal(e);
			}
		}
		return _analyzerClazz;
	}

	public boolean isAnalyzer() {
		if (_analyzerClazz != null) {
			return _analyzerClazz.isAnnotationPresent(AnalyzerBean.class);
		}
		return false;
	}

	public Class<?> getAnalyzerClass() {
		return _analyzerClazz;
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