package org.eobjects.analyzer.beans.similarity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.result.AnalyzerResult;

public class SimilarityResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private Class<? extends Analyzer> _analyzerBeanClass;
	private Set<SimilarValues> _similarValues;

	public SimilarityResult(Class<? extends Analyzer> analyzerBeanClass,
			Set<SimilarValues> similarValues) {
		_analyzerBeanClass = analyzerBeanClass;
		_similarValues = similarValues;

	}

	@Override
	public Class<? extends Analyzer> getProducerClass() {
		return _analyzerBeanClass;
	}

	public Set<SimilarValues> getSimilarValues() {
		return Collections.unmodifiableSet(_similarValues);
	}

	public Set<String> getValues() {
		Set<String> result = new HashSet<String>();
		for (SimilarValues sv : _similarValues) {
			String[] values = sv.getValues();
			result.add(values[0]);
			result.add(values[1]);
		}
		return result;
	}

	public Set<String> getSimilarValues(String value) {
		Set<String> result = new HashSet<String>();
		for (SimilarValues sv : _similarValues) {
			if (sv.contains(value)) {
				String[] values = sv.getValues();
				if (value.equals(values[0])) {
					result.add(values[1]);
				} else {
					result.add(values[0]);
				}
			}
		}
		return result;
	}
}
