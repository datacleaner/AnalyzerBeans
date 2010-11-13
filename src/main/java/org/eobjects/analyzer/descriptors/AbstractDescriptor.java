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
