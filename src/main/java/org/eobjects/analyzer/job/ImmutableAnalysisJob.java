package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.data.InputColumn;

public class ImmutableAnalysisJob implements AnalysisJob {

	private DataContextProvider _dataContextProvider;
	private Collection<InputColumn<?>> _sourceColumns;
	private Collection<TransformerJob> _transformerJobs;
	private Collection<AnalyzerJob> _analyzerJobs;

	public ImmutableAnalysisJob(DataContextProvider dataContextProvider,
			Collection<? extends InputColumn<?>> sourceColumns,
			Collection<TransformerJob> transformerJobs,
			Collection<AnalyzerJob> analyzerJobs) {
		_dataContextProvider = dataContextProvider;
		_sourceColumns = Collections
				.unmodifiableList(new ArrayList<InputColumn<?>>(sourceColumns));
		_transformerJobs = Collections
				.unmodifiableList(new ArrayList<TransformerJob>(transformerJobs));
		_analyzerJobs = Collections
				.unmodifiableList(new ArrayList<AnalyzerJob>(analyzerJobs));
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		return _dataContextProvider;
	}

	@Override
	public Collection<InputColumn<?>> getSourceColumns() {
		return _sourceColumns;
	}

	@Override
	public Collection<TransformerJob> getTransformerJobs() {
		return _transformerJobs;
	}

	@Override
	public Collection<AnalyzerJob> getAnalyzerJobs() {
		return _analyzerJobs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_analyzerJobs == null) ? 0 : _analyzerJobs.hashCode());
		result = prime * result
				+ ((_sourceColumns == null) ? 0 : _sourceColumns.hashCode());
		result = prime
				* result
				+ ((_transformerJobs == null) ? 0 : _transformerJobs.hashCode());
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
		ImmutableAnalysisJob other = (ImmutableAnalysisJob) obj;
		if (_analyzerJobs == null) {
			if (other._analyzerJobs != null)
				return false;
		} else if (!_analyzerJobs.equals(other._analyzerJobs))
			return false;
		if (_sourceColumns == null) {
			if (other._sourceColumns != null)
				return false;
		} else if (!_sourceColumns.equals(other._sourceColumns))
			return false;
		if (_transformerJobs == null) {
			if (other._transformerJobs != null)
				return false;
		} else if (!_transformerJobs.equals(other._transformerJobs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableAnalysisJob[sourceColumns=" + _sourceColumns.size()
				+ ",transformerJobs=" + _transformerJobs.size()
				+ ",analyzerJobs=" + _analyzerJobs.size() + "]";
	}
}
