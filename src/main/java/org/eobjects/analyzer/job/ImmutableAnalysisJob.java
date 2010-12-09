/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;

public final class ImmutableAnalysisJob implements AnalysisJob {

	private final Datastore _datastore;
	private final Collection<InputColumn<?>> _sourceColumns;
	private final Collection<TransformerJob> _transformerJobs;
	private final Collection<AnalyzerJob> _analyzerJobs;
	private final Collection<FilterJob> _filterJobs;
	private final Collection<MergedOutcomeJob> _mergedOutcomeJobs;

	public ImmutableAnalysisJob(Datastore datastore, Collection<? extends InputColumn<?>> sourceColumns,
			Collection<FilterJob> filterJobs, Collection<TransformerJob> transformerJobs,
			Collection<AnalyzerJob> analyzerJobs, Collection<MergedOutcomeJob> mergedOutcomeJobs) {
		_datastore = datastore;
		_sourceColumns = Collections.unmodifiableList(new ArrayList<InputColumn<?>>(sourceColumns));
		_transformerJobs = Collections.unmodifiableList(new ArrayList<TransformerJob>(transformerJobs));
		_analyzerJobs = Collections.unmodifiableList(new ArrayList<AnalyzerJob>(analyzerJobs));
		_filterJobs = Collections.unmodifiableList(new ArrayList<FilterJob>(filterJobs));
		_mergedOutcomeJobs = Collections.unmodifiableList(new ArrayList<MergedOutcomeJob>(mergedOutcomeJobs));
	}

	@Override
	public Datastore getDatastore() {
		return _datastore;
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
	public Collection<FilterJob> getFilterJobs() {
		return _filterJobs;
	}

	@Override
	public Collection<MergedOutcomeJob> getMergedOutcomeJobs() {
		return _mergedOutcomeJobs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;

		int result = 1;
		result = prime * result + ((_analyzerJobs == null) ? 0 : _analyzerJobs.hashCode());
		result = prime * result + ((_sourceColumns == null) ? 0 : _sourceColumns.hashCode());
		result = prime * result + ((_transformerJobs == null) ? 0 : _transformerJobs.hashCode());
		result = prime * result + ((_filterJobs == null) ? 0 : _filterJobs.hashCode());
		result = prime * result + ((_mergedOutcomeJobs == null) ? 0 : _mergedOutcomeJobs.hashCode());
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
		if (_filterJobs == null) {
			if (other._filterJobs != null)
				return false;
		} else if (!_filterJobs.equals(other._filterJobs))
			return false;
		if (_mergedOutcomeJobs == null) {
			if (other._mergedOutcomeJobs != null)
				return false;
		} else if (!_mergedOutcomeJobs.equals(other._mergedOutcomeJobs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableAnalysisJob[sourceColumns=" + _sourceColumns.size() + ",filterJobs=" + _filterJobs.size()
				+ ",transformerJobs=" + _transformerJobs.size() + ",analyzerJobs=" + _analyzerJobs.size()
				+ ",mergedOutcomeJobs=" + _mergedOutcomeJobs.size() + "]";
	}
}
