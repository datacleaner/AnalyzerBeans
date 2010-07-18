package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.data.InputColumn;

public class ImmutableAnalysisJob implements AnalysisJob {

	private transient DataContextProvider _dataContextProvider;
	private transient Collection<InputColumn<?>> _sourceColumns;
	private Collection<TransformerJob> _transformerJobs;
	private Collection<AnalyzerJob> _analyzerJobs;

	public ImmutableAnalysisJob(DataContextProvider dataContextProvider,
			Collection<? extends InputColumn<?>> sourceColumns,
			Collection<TransformerJob> transformerJobs,
			Collection<AnalyzerJob> analyzerJobs) {
		_dataContextProvider = dataContextProvider;
		_sourceColumns = new ArrayList<InputColumn<?>>(sourceColumns);
		_transformerJobs = new ArrayList<TransformerJob>(transformerJobs);
		_analyzerJobs = new ArrayList<AnalyzerJob>(analyzerJobs);
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

}
