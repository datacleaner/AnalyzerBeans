package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.util.ReflectionUtils;

public class AnalyzerBeanDescriptor extends AbstractBeanDescriptor {

	private String displayName;
	private boolean exploringAnalyzer;
	private boolean rowProcessingAnalyzer;

	public AnalyzerBeanDescriptor(Class<?> analyzerClass)
			throws DescriptorException {
		super(analyzerClass, ReflectionUtils.is(analyzerClass,
				RowProcessingAnalyzer.class));

		rowProcessingAnalyzer = ReflectionUtils.is(analyzerClass,
				RowProcessingAnalyzer.class);
		exploringAnalyzer = ReflectionUtils.is(analyzerClass,
				ExploringAnalyzer.class);

		if (!rowProcessingAnalyzer && !exploringAnalyzer) {
			throw new DescriptorException(analyzerClass
					+ " does not implement either "
					+ RowProcessingAnalyzer.class.getName() + " or "
					+ ExploringAnalyzer.class.getName());
		}

		AnalyzerBean analyzerAnnotation = analyzerClass
				.getAnnotation(AnalyzerBean.class);
		if (analyzerAnnotation == null) {
			throw new DescriptorException(analyzerClass
					+ " doesn't implement the AnalyzerBean annotation");
		}

		displayName = analyzerAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(
					analyzerClass.getSimpleName(), false);
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isExploringAnalyzer() {
		return exploringAnalyzer;
	}

	public boolean isRowProcessingAnalyzer() {
		return rowProcessingAnalyzer;
	}

	@Override
	public ConfiguredDescriptor getConfiguredDescriptorForInput() {
		if (isRowProcessingAnalyzer()) {
			return super.getConfiguredDescriptorForInput();
		}
		return null;
	}
}