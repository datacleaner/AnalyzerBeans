package org.eobjects.analyzer.beans.similarity;

import java.io.Serializable;
import java.util.Arrays;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.CompareUtils;

/**
 * Represents a group of values that have been marked as similar, typically by
 * an analyzer which does some kind of fuzzy matching of values.
 * 
 * @author Kasper Sørensen
 */
public final class SimilarityGroup implements Serializable, Comparable<SimilarityGroup> {

	private static final long serialVersionUID = 1L;

	private final RowAnnotationFactory _annotationFactory;
	private final RowAnnotation _annotation;
	private final InputColumn<?> _column;
	private final String[] _values;

	public SimilarityGroup(String value, String... values) {
		this(null, null, null, value, values);
	}

	public SimilarityGroup(RowAnnotation annotation, RowAnnotationFactory annotationFactory, InputColumn<String> column,
			String value, String... values) {
		_annotation = annotation;
		_annotationFactory = annotationFactory;
		_column = column;
		_values = CollectionUtils.array(values, value);

		// ensure that any given two arguments will be placed in the same order
		// internally, regardless of the order of the arguments
		Arrays.sort(_values);
	}

	@Override
	public int hashCode() {
		int hashCode = -100 * _values.length;
		for (String value : _values) {
			hashCode += value.hashCode();
		}
		return hashCode;
	}

	public AnnotatedRowsResult getAnnotatedRows() {
		return new AnnotatedRowsResult(_annotation, _annotationFactory, _column);
	}

	public RowAnnotation getAnnotation() {
		return _annotation;
	}

	public boolean contains(String s) {
		if (s != null) {
			for (String value : _values) {
				if (s.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass() == getClass()) {
			SimilarityGroup that = (SimilarityGroup) obj;
			return CompareUtils.equals(_values, that._values);
		}
		return false;
	}

	public String[] getValues() {
		return _values;
	}

	public int getValueCount() {
		return _values.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SimilarValues[");
		for (int i = 0; i < _values.length; i++) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append(_values[i]);
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public int compareTo(SimilarityGroup that) {
		String[] values = that.getValues();
		for (int i = 0; i < Math.min(_values.length, values.length); i++) {
			int result = _values[i].compareTo(values[i]);
			if (result != 0) {
				return result;
			}
		}
		return _values.length - values.length;
	}

}
