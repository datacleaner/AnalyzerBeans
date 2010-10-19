package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public final class ProvidedPropertyDescriptorImpl extends AbstractPropertyDescriptor implements ProvidedPropertyDescriptor {

	public ProvidedPropertyDescriptorImpl(Field field, BeanDescriptor<?> beanDescriptor) throws DescriptorException {
		super(field, beanDescriptor);
	}

	@Override
	public boolean isSet() {
		return ReflectionUtils.isSet(getBaseType());
	}

	@Override
	public boolean isList() {
		return ReflectionUtils.isList(getBaseType());
	}

	@Override
	public boolean isMap() {
		return ReflectionUtils.isMap(getBaseType());
	}

	@Override
	public boolean isSchemaNavigator() {
		return getBaseType() == SchemaNavigator.class;
	}

	@Override
	public boolean isDataContext() {
		return getBaseType() == DataContext.class;
	}
}
