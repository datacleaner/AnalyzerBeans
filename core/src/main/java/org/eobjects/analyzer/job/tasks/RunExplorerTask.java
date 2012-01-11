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
package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.metamodel.DataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RunExplorerTask implements Task {

	private static final Logger logger = LoggerFactory.getLogger(RunExplorerTask.class);

	private final Explorer<?> _explorer;
	private final AnalysisJob _job;
	private final ExplorerJob _explorerJob;
	private final Datastore _datastore;
	private final AnalysisListener _analysisListener;

	public RunExplorerTask(Explorer<?> explorer, AnalysisJob job, ExplorerJob explorerJob, Datastore datastore,
			AnalysisListener analysisListener) {
		_explorer = explorer;
		_job = job;
		_explorerJob = explorerJob;
		_datastore = datastore;
		_analysisListener = analysisListener;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");

		if (_analysisListener != null) {
			_analysisListener.explorerBegin(_job, _explorerJob);
		}
		DatastoreConnection con = _datastore.openConnection();
		DataContext dc = con.getDataContext();
		try {
			_explorer.run(dc);
		} finally {
			con.close();
		}
	}
}
