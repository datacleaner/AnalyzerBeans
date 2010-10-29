package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.eobjects.analyzer.util.ReflectionUtils;

public abstract class AbstractDescriptor<B> {

	private final Class<B> _beanClass;

	public AbstractDescriptor(Class<B> beanClass) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass cannot be null");
		}
		if (beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers())) {
			throw new DescriptorException("Bean (" + beanClass + ") is not a non-abstract class");
		}

		_beanClass = beanClass;
	}
	
	protected void visitClass() {
		Field[] fields = ReflectionUtils.getFields(_beanClass);
		for (Field field : fields) {
			visitField(field);
		}
		
		Method[] methods = ReflectionUtils.getMethods(_beanClass);
		for (Method method : methods) {
			visitMethod(method);
		}
	}

	protected abstract void visitField(Field field);

	protected abstract void visitMethod(Method method);

	public Class<B> getBeanClass() {
		return _beanClass;
	}

	@Override
	public int hashCode() {
		return _beanClass.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj.getClass() == this.getClass()) {
			AbstractDescriptor<?> that = (AbstractDescriptor<?>) obj;
			return this._beanClass == that._beanClass;
		}
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[beanClass=" + _beanClass.getName() + "]";
	}
}
