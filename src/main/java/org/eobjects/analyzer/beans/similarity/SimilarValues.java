package org.eobjects.analyzer.beans.similarity;

import java.io.Serializable;

public class SimilarValues implements Serializable, Comparable<SimilarValues> {

	private static final long serialVersionUID = 1L;

	private String _s1;
	private String _s2;

	public SimilarValues(String s1, String s2) {
		// ensure that any given two arguments will be placed in the same order
		// internally, regardless of the order of the arguments
		if (s1.compareTo(s2) < 0) {
			_s1 = s1;
			_s2 = s2;
		} else {
			// reversed order
			_s1 = s2;
			_s2 = s1;
		}
	}

	@Override
	public int hashCode() {
		return _s1.hashCode() + _s2.hashCode();
	}

	public boolean contains(String s) {
		if (s != null) {
			return s.equals(_s1) || s.equals(_s2);
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == SimilarValues.class) {
			SimilarValues that = (SimilarValues) obj;
			return that._s1.equals(this._s1) && that._s2.equals(this._s2);
		}
		return super.equals(obj);
	}

	public String[] getValues() {
		return new String[] { _s1, _s2 };
	}

	@Override
	public String toString() {
		return "SimilarValues[" + _s1 + "," + _s2 + "]";
	}

	@Override
	public int compareTo(SimilarValues that) {
		int result = this._s1.compareTo(that._s1);
		if (result == 0) {
			result = this._s2.compareTo(that._s2);
		}
		return result;
	}

}
