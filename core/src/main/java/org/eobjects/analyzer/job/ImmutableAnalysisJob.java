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
import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;

import org.eobjects.metamodel.util.BaseObject;

public final class ImmutableAnalysisJob extends BaseObject implements AnalysisJob {

	private final Datastore _datastore;
	private final List<InputColumn<?>> _sourceColumns;
	private final List<TransformerJob> _transformerJobs;
	private final List<AnalyzerJob> _analyzerJobs;
	private final List<FilterJob> _filterJobs;
	private final List<MergedOutcomeJob> _mergedOutcomeJobs;

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
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_datastore);
		identifiers.add(_sourceColumns);
		identifiers.add(_transformerJobs);
		identifiers.add(_analyzerJobs);
		identifiers.add(_filterJobs);
		identifiers.add(_mergedOutcomeJobs);
	}

	@Override
	public Datastore getDatastore() {
		return _datastore;
	}
	
	@Override
	public List<InputColumn<?>> getSourceColumns() {
		return _sourceColumns;
	}

	@Override
	public List<TransformerJob> getTransformerJobs() {
		return _transformerJobs;
	}

	@Override
	public List<AnalyzerJob> getAnalyzerJobs() {
		return _analyzerJobs;
	}

	@Override
	public List<FilterJob> getFilterJobs() {
		return _filterJobs;
	}

	@Override
	public List<MergedOutcomeJob> getMergedOutcomeJobs() {
		return _mergedOutcomeJobs;
	}

	@Override
	public String toString() {
		return "ImmutableAnalysisJob[sourceColumns=" + _sourceColumns.size() + ",filterJobs=" + _filterJobs.size()
				+ ",transformerJobs=" + _transformerJobs.size() + ",analyzerJobs=" + _analyzerJobs.size()
				+ ",mergedOutcomeJobs=" + _mergedOutcomeJobs.size() + "]";
	}

}
