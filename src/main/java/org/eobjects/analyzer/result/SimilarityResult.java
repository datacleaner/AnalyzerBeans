package org.eobjects.analyzer.result;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.similarity.SimilarValues;

public class SimilarityResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private Set<SimilarValues> _similarValues;

	public SimilarityResult(Set<SimilarValues> similarValues) {
		_similarValues = similarValues;

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
