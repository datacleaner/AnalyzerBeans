package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.beans.api.Analyzer;

public interface AnalyzerBeanDescriptor<B extends Analyzer<?>> extends BeanDescriptor<B> {

	public boolean isExploringAnalyzer();

	public boolean isRowProcessingAnalyzer();
}
