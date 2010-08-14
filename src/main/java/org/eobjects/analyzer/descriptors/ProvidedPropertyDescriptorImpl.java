package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public final class ProvidedPropertyDescriptorImpl extends AbstractPropertyDescriptor implements
		ProvidedPropertyDescriptor {

	public ProvidedPropertyDescriptorImpl(Field field) throws DescriptorException {
		super(field);
	}

	public boolean isList() {
		return ReflectionUtils.isList(getBaseType());
	}

	public boolean isMap() {
		return ReflectionUtils.isMap(getBaseType());
	}

	public boolean isSchemaNavigator() {
		return getBaseType() == SchemaNavigator.class;
	}

	public boolean isDataContext() {
		return getBaseType() == DataContext.class;
	}
}
