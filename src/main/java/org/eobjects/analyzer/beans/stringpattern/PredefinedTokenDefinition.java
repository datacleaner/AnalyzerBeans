package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PredefinedTokenDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private String _name;
	private Set<String> _tokenRegexes;
	private Set<Pattern> _tokenRegexPatterns;

	public PredefinedTokenDefinition(String name, Set<String> tokenRegexes) {
		_name = name;
		_tokenRegexes = tokenRegexes;
	}

	public PredefinedTokenDefinition(String name, String... tokenRegexes) {
		_name = name;
		_tokenRegexes = new LinkedHashSet<String>();
		for (String string : tokenRegexes) {
			_tokenRegexes.add(string);
		}
	}

	public String getName() {
		return _name;
	}

	public Set<String> getTokenRegexes() {
		return Collections.unmodifiableSet(_tokenRegexes);
	}

	public Set<Pattern> getTokenRegexPatterns() {
		if (_tokenRegexPatterns == null) {
			_tokenRegexPatterns = new LinkedHashSet<Pattern>();
			for (String tokenRegex : _tokenRegexes) {
				Pattern pattern = Pattern.compile(tokenRegex);
				_tokenRegexPatterns.add(pattern);
			}
		}
		return _tokenRegexPatterns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredefinedTokenDefinition other = (PredefinedTokenDefinition) obj;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PredefinedToken[name=" + _name + "]";
	}
}
