package org.eobjects.analyzer.util;

import java.util.Map;

public class NamedPatternMatch<E extends Enum<E>> {

	private Map<E, String> resultMap;

	public NamedPatternMatch(Map<E, String> resultMap) {
		this.resultMap = resultMap;
	}

	public String get(E group) {
		return resultMap.get(group);
	}
}
