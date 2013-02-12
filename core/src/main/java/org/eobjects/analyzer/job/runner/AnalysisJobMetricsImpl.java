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
package org.eobjects.analyzer.job.runner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Ref;

final class AnalysisJobMetricsImpl implements AnalysisJobMetrics {

	private final AnalysisJob _job;
	private final RowProcessingPublishers _publishers;
	private final Map<Table, RowProcessingMetrics> _rowProcessingMetrics;

	public AnalysisJobMetricsImpl(AnalysisJob job, RowProcessingPublishers publishers) {
		_job = job;
		_publishers = publishers;
		_rowProcessingMetrics = createRowProcessingMetrics();
	}

	@Override
	public AnalysisJob getAnalysisJob() {
		return _job;
	}

	@Override
	public ExplorerMetrics getExplorerMetrics(ExplorerJob explorerJob) {
		return new ExplorerMetricsImpl(this, explorerJob);
	}

	@Override
	public AnalyzerMetrics getAnalyzerMetrics(AnalyzerJob analyzerJob) {
		Table table = getRowProcessingTable(analyzerJob);
		RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics(table);
		return new AnalyzerMetricsImpl(rowProcessingMetrics, analyzerJob);
	}

	@Override
	public Table[] getRowProcessingTables() {
		Set<Table> tables = _rowProcessingMetrics.keySet();
		return tables.toArray(new Table[tables.size()]);
	}

	@Override
	public RowProcessingMetrics getRowProcessingMetrics(Table table) {
		return _rowProcessingMetrics.get(table);
	}

	@Override
	public Table getRowProcessingTable(AnalyzerJob analyzerJob) {
		Table[] tables = _publishers.getTables(analyzerJob);
		// this should always work for analyzers
		return tables[0];
	}

	private Map<Table, RowProcessingMetrics> createRowProcessingMetrics() {
		final Map<Table, RowProcessingMetrics> map = new HashMap<Table, RowProcessingMetrics>();
		final Table[] tables = _publishers.getTables();

		for (Table table : tables) {
			final RowProcessingPublisher publisher = _publishers.getRowProcessingPublisher(table);
			final AnalyzerJob[] analyzerJobs = publisher.getAnalyzerJobs();
			final Ref<Query> queryRef = new LazyRef<Query>() {
				@Override
				protected Query fetch() {
					return publisher.getQuery();
				}
			};

			final RowProcessingMetricsImpl metrics = new RowProcessingMetricsImpl(this, table, analyzerJobs, queryRef);
			map.put(table, metrics);
		}

		return map;
	}
}
