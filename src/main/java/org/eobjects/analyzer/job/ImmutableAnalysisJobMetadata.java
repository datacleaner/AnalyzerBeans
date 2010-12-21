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
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ImmutableAnalysisJobMetadata implements AnalysisJobMetadata {

	private final String _jobName;
	private final String _jobVersion;
	private final String _jobDescription;
	private final String _author;
	private final Date _createdDate;
	private final Date _updatedDate;
	private final String _datastoreName;
	private final List<String> _sourceColumnPaths;

	public ImmutableAnalysisJobMetadata(String jobName, String jobVersion, String jobDescription, String author,
			Date createdDate, Date updatedDate, String datastoreName, List<String> sourceColumnPaths) {
		_jobName = jobName;
		_jobVersion = jobVersion;
		_jobDescription = jobDescription;
		_author = author;
		_createdDate = createdDate;
		_updatedDate = updatedDate;
		_datastoreName = datastoreName;
		if (sourceColumnPaths == null) {
			sourceColumnPaths = Collections.emptyList();
		} else {
			sourceColumnPaths = new ArrayList<String>(sourceColumnPaths);
		}
		_sourceColumnPaths = Collections.unmodifiableList(sourceColumnPaths);
	}

	@Override
	public String getJobName() {
		return _jobName;
	}

	@Override
	public String getJobVersion() {
		return _jobVersion;
	}

	@Override
	public String getJobDescription() {
		return _jobDescription;
	}

	@Override
	public String getAuthor() {
		return _author;
	}

	@Override
	public Date getCreatedDate() {
		return _createdDate;
	}

	@Override
	public Date getUpdatedDate() {
		return _updatedDate;
	}

	@Override
	public String getDatastoreName() {
		return _datastoreName;
	}

	@Override
	public List<String> getSourceColumnPaths() {
		return _sourceColumnPaths;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_author == null) ? 0 : _author.hashCode());
		result = prime * result + ((_createdDate == null) ? 0 : _createdDate.hashCode());
		result = prime * result + ((_datastoreName == null) ? 0 : _datastoreName.hashCode());
		result = prime * result + ((_jobDescription == null) ? 0 : _jobDescription.hashCode());
		result = prime * result + ((_jobName == null) ? 0 : _jobName.hashCode());
		result = prime * result + ((_jobVersion == null) ? 0 : _jobVersion.hashCode());
		result = prime * result + ((_sourceColumnPaths == null) ? 0 : _sourceColumnPaths.hashCode());
		result = prime * result + ((_updatedDate == null) ? 0 : _updatedDate.hashCode());
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
		ImmutableAnalysisJobMetadata other = (ImmutableAnalysisJobMetadata) obj;
		if (_author == null) {
			if (other._author != null)
				return false;
		} else if (!_author.equals(other._author))
			return false;
		if (_createdDate == null) {
			if (other._createdDate != null)
				return false;
		} else if (!_createdDate.equals(other._createdDate))
			return false;
		if (_datastoreName == null) {
			if (other._datastoreName != null)
				return false;
		} else if (!_datastoreName.equals(other._datastoreName))
			return false;
		if (_jobDescription == null) {
			if (other._jobDescription != null)
				return false;
		} else if (!_jobDescription.equals(other._jobDescription))
			return false;
		if (_jobName == null) {
			if (other._jobName != null)
				return false;
		} else if (!_jobName.equals(other._jobName))
			return false;
		if (_jobVersion == null) {
			if (other._jobVersion != null)
				return false;
		} else if (!_jobVersion.equals(other._jobVersion))
			return false;
		if (_sourceColumnPaths == null) {
			if (other._sourceColumnPaths != null)
				return false;
		} else if (!_sourceColumnPaths.equals(other._sourceColumnPaths))
			return false;
		if (_updatedDate == null) {
			if (other._updatedDate != null)
				return false;
		} else if (!_updatedDate.equals(other._updatedDate))
			return false;
		return true;
	}
}
