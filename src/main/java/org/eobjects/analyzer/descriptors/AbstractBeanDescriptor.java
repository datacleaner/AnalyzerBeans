package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.data.DataTypeFamily;

public abstract class AbstractBeanDescriptor implements
		Comparable<AbstractBeanDescriptor> {

	private Class<?> beanClass;
	protected List<CloseDescriptor> closeDescriptors = new LinkedList<CloseDescriptor>();
	protected List<ConfiguredDescriptor> configuredDescriptors = new LinkedList<ConfiguredDescriptor>();
	protected List<InitializeDescriptor> initializeDescriptors = new LinkedList<InitializeDescriptor>();
	protected List<ProvidedDescriptor> providedDescriptors = new LinkedList<ProvidedDescriptor>();

	public AbstractBeanDescriptor(Class<?> beanClass) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass cannot be null");
		}
		this.beanClass = beanClass;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public List<InitializeDescriptor> getInitializeDescriptors() {
		return initializeDescriptors;
	}

	public List<ProvidedDescriptor> getProvidedDescriptors() {
		return providedDescriptors;
	}

	public List<ConfiguredDescriptor> getConfiguredDescriptors() {
		return configuredDescriptors;
	}

	public ConfiguredDescriptor getConfiguredDescriptor(String configuredName) {
		for (ConfiguredDescriptor configuredDescriptor : configuredDescriptors) {
			if (configuredName.equals(configuredDescriptor.getName())) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	public List<CloseDescriptor> getCloseDescriptors() {
		return closeDescriptors;
	}

	public ConfiguredDescriptor getConfiguredDescriptorForInput() {
		List<ConfiguredDescriptor> descriptors = getConfiguredDescriptors();
		for (ConfiguredDescriptor configuredDescriptor : descriptors) {
			if (configuredDescriptor.isInputColumn()) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	public DataTypeFamily getInputDataTypeFamily() {
		ConfiguredDescriptor configuredDescriptor = getConfiguredDescriptorForInput();
		if (configuredDescriptor == null) {
			return DataTypeFamily.UNDEFINED;
		}
		Type genericType = configuredDescriptor.getGenericType();
		Class<?> typeParameter = AnnotationHelper.getTypeParameter(genericType,
				0);
		return DataTypeFamily.valueOf(typeParameter);
	}

	@Override
	public int hashCode() {
		return beanClass.hashCode();
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
			AbstractBeanDescriptor that = (AbstractBeanDescriptor) obj;
			return this.beanClass == that.beanClass;
		}
		return false;
	}

	@Override
	public int compareTo(AbstractBeanDescriptor o) {
		String thisAnalyzerClassName = this.getBeanClass().toString();
		String thatAnalyzerClassName = o.getBeanClass().toString();
		return thisAnalyzerClassName.compareTo(thatAnalyzerClassName);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[beanClass=" + beanClass + "]";
	}
}
