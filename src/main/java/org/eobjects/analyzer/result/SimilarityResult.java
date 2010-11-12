package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.beans.similarity.SimilarityGroup;

public class SimilarityResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private List<SimilarityGroup> _similarityGroups;

	public SimilarityResult(List<SimilarityGroup> similarityGroups) {
		_similarityGroups = similarityGroups;

	}

	public List<SimilarityGroup> getSimilarityGroups() {
		return _similarityGroups;
	}

	public Set<String> getValues() {
		Set<String> result = new HashSet<String>();
		for (SimilarityGroup sv : _similarityGroups) {
			String[] values = sv.getValues();
			result.add(values[0]);
			result.add(values[1]);
		}
		return result;
	}

	public List<String> getSimilarValues(String string) {
		ArrayList<String> result = new ArrayList<String>();
		for (SimilarityGroup similarityGroup : _similarityGroups) {
			if (similarityGroup.contains(string)) {
				String[] values = similarityGroup.getValues();
				for (String value : values) {
					if (!value.equals(string)) {
						result.add(value);
					}
				}
				break;
			}
		}
		return result;
	}

}
