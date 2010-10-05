package org.eobjects.analyzer.result;

import java.util.List;

import org.eobjects.analyzer.beans.api.Analyzer;

/**
 * A very simple AnalyzerResult that simply holds a list of values
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public class ListResult<E> implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private Class<? extends Analyzer<?>> _producerClass;
	private List<E> _values;

	public ListResult(List<E> values, Class<? extends Analyzer<?>> producerClass) {
		_values = values;
		_producerClass = producerClass;
	}

	@Override
	public Class<? extends Analyzer<?>> getProducerClass() {
		return _producerClass;
	}

	public List<E> getValues() {
		return _values;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (E value : _values) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(value);
		}
		return sb.toString();
	}
}
