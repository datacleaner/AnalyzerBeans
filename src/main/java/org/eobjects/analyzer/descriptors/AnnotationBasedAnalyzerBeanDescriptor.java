package org.eobjects.analyzer.descriptors;

import java.util.Set;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class AnnotationBasedAnalyzerBeanDescriptor<A extends Analyzer<?>> extends AbstractBeanDescriptor<A> implements
		AnalyzerBeanDescriptor<A> {

	private final String _displayName;
	private final boolean _exploringAnalyzer;
	private final boolean _rowProcessingAnalyzer;

	public static <A extends Analyzer<?>> AnnotationBasedAnalyzerBeanDescriptor<A> create(Class<A> analyzerClass) {
		return new AnnotationBasedAnalyzerBeanDescriptor<A>(analyzerClass);
	}

	private AnnotationBasedAnalyzerBeanDescriptor(Class<A> analyzerClass) throws DescriptorException {
		super(analyzerClass, ReflectionUtils.is(analyzerClass, RowProcessingAnalyzer.class));

		_rowProcessingAnalyzer = ReflectionUtils.is(analyzerClass, RowProcessingAnalyzer.class);
		_exploringAnalyzer = ReflectionUtils.is(analyzerClass, ExploringAnalyzer.class);

		if (!_rowProcessingAnalyzer && !_exploringAnalyzer) {
			throw new DescriptorException(analyzerClass + " does not implement either "
					+ RowProcessingAnalyzer.class.getName() + " or " + ExploringAnalyzer.class.getName());
		}

		AnalyzerBean analyzerAnnotation = analyzerClass.getAnnotation(AnalyzerBean.class);
		if (analyzerAnnotation == null) {
			throw new DescriptorException(analyzerClass + " doesn't implement the AnalyzerBean annotation");
		}

		String displayName = analyzerAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(analyzerClass.getSimpleName(), false);
		}
		_displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	@Override
	public boolean isExploringAnalyzer() {
		return _exploringAnalyzer;
	}

	@Override
	public boolean isRowProcessingAnalyzer() {
		return _rowProcessingAnalyzer;
	}

	@Override
	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput() {
		if (isRowProcessingAnalyzer()) {
			return super.getConfiguredPropertiesForInput();
		}
		return null;
	}
}
