package org.eobjects.analyzer.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;

public class AnnotationVisitor implements ClassVisitor {

	private final static Log _log = LogFactory.getLog(AnnotationVisitor.class);
	public static final String ANALYZER_DESC = 'L' + AnalyzerBean.class.getCanonicalName().replace('.', '/') + ';';
	private Class<?> _analyzerClazz;
	private String _name;

	@Override
	public void visit(int version, int access, String name, String superName, String[] interfaces, String sourceFile) {
		_log.info("Visiting class: " + name);
		_name = name;
	}

	public String getName() {
		return _name;
	}

	@Override
	public void visitAttribute(Attribute attribute) {
		while (attribute != null && _analyzerClazz == null) {
			if ("RuntimeVisibleAnnotations".equals(attribute.type)) {
				initializeClass();
			}

			// Iterate
			attribute = attribute.next;
		}
	}

	@Override
	public void visitEnd() {
	}

	@Override
	public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
	}

	@Override
	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
	}

	@Override
	public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
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
}